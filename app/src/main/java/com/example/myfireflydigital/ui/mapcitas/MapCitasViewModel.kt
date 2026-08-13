package com.example.myfireflydigital.ui.mapcitas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfireflydigital.R
import com.example.myfireflydigital.domain.exceptions.GeoLocationResolvableException
import com.example.myfireflydigital.domain.exceptions.toUiText
import com.example.myfireflydigital.domain.model.AppMessage
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.RouteResult
import com.example.myfireflydigital.domain.model.result.EstadoCita
import com.example.myfireflydigital.domain.usecase.GetCitasObserverUseCase
import com.example.myfireflydigital.domain.usecase.GetCurrentLocationUseCase
import com.example.myfireflydigital.domain.usecase.GetRouteUseCase
import com.example.myfireflydigital.domain.usecase.UpsertCitaUseCase
import com.example.myfireflydigital.domain.util.UiText
import com.example.myfireflydigital.ui.modeloui.MapCitasEvent
import com.example.myfireflydigital.ui.modeloui.MapUiState
import com.example.myfireflydigital.ui.modeloui.UiEffect
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapCitasViewModel @Inject constructor(
    private val getCurrenLocationUseCase: GetCurrentLocationUseCase,
    private val getCitasObserverUseCase: GetCitasObserverUseCase,
    private val getRouteUseCase: GetRouteUseCase,
    private val upsertCitaUseCase: UpsertCitaUseCase
) : ViewModel() {
    private val _uiMapState = MutableStateFlow(MapUiState())
    //val uiMapState : StateFlow<MapUiState> = _uiMapState.asStateFlow()

    val uiMapState: StateFlow<MapUiState> = combine(
        _uiMapState,
        getCitasObserverUseCase()
    ) { uiState, citasFromDb ->
        uiState.copy(citas = citasFromDb, isCitasLoaded = true)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState(/*isCitasLoaded = false, isMapLoaded = false*/)
    )
    private val _effect = Channel<UiEffect>(Channel.CONFLATED)//SOLO GUARDA EL ULTIMO AVISO
    val effect = _effect.receiveAsFlow()

    private var routerJob: Job? = null //DEBOUNCE

    fun onEvent(event: MapCitasEvent) {
        when (event) {
            MapCitasEvent.OnMapLoaded -> _uiMapState.update { it.copy(isMapLoaded = true) }
            MapCitasEvent.OnMyLocation -> onMyLocation()
            is MapCitasEvent.OnSelectCita -> onSelectCita(event.cita)
            MapCitasEvent.OnClearRoute -> onClearRoute()
            is MapCitasEvent.OnCancelarCita -> onCancelarCita(event.citaId)
            MapCitasEvent.OnMyLocationButtonClick -> onMyLocationButtonClick()
        }
    }

    private fun onMyLocation() {
        viewModelScope.launch {
            //_uiMapState.update { it.copy(isLoading = true) }
            getCurrenLocationUseCase().onSuccess { latLng ->
                _uiMapState.update {
                    it.copy(
                        userLocation = latLng,
                        locationUpdateTick = it.locationUpdateTick + 1,
                        //properties = it.properties.copy(isMyLocationEnabled = true)
                    )
                }
            }.onFailure { error ->
                when(error){
                    is GeoLocationResolvableException ->{
                        Log.d("MapCitasViewModel", "onMyLocation_1: ${error.message}")
                        //_uiMapState.update { it.copy(properties = it.properties.copy(isMyLocationEnabled = true)) }
                        //val request = IntentSenderRequest.Builder(error.intentSender).build()
                        _effect.send(UiEffect.RequestGpsEnable(error.intentSender))
                    }
                    else -> _effect.send(UiEffect.ShowSnackbar(AppMessage.Error(error.toUiText())))
                }
                Log.d("MapCitasViewModel", "onMyLocation_2: ${error.message}")
            }
        }
    }

    private fun onMyLocationButtonClick(){
        onMyLocation()
    }

    private fun onSelectCita(cita: Cita) {
        val userLocation = _uiMapState.value.userLocation ?: run {
            _effect.trySend(UiEffect.ShowSnackbar(AppMessage.Warning(UiText.StringResource(R.string.error_geo_location))))
            return
        }
        //if (_uiMapState.value.citaSelecId == cita.id) onClearRoute()
        //CANCELA si el user cambio de cita rapidamente antes que responda la peticion de getRouteUseCase()
        Log.d("MapCitasViewModel", "onSelectCita: ${cita.id}")
        routerJob?.cancel()
        routerJob = viewModelScope.launch {
            _uiMapState.update {
                it.copy(
                    isLoadingRouteUbi = true,
                    citaSelecId = cita.id,
                    routeInfo = null
                )
            }
            getRouteUseCase(userLocation, LatLng(cita.latitud, cita.longitud))
                .onSuccess { routeInfo ->
                    _uiMapState.update { it.copy(isLoadingRouteUbi = false, routeInfo = RouteResult(routeInfo.routePoints, routeInfo.distance, routeInfo.duration)) }
                }.onFailure {
                    _uiMapState.update { it.copy(isLoadingRouteUbi = false) }
                    _effect.send(UiEffect.ShowSnackbar(AppMessage.Error(it.toUiText())))
                }
        }
    }

    private fun onClearRoute() {
        routerJob?.cancel()
        _uiMapState.update {
            it.copy(
                citaSelecId = null,
                isLoadingRouteUbi = false,
                routeInfo = null
            )
        }
    }

    private fun onCancelarCita(citaId: Int){
        val cita = uiMapState.value.citas.find { it.id == citaId } ?: return
        viewModelScope.launch {
            upsertCitaUseCase(cita.copy(estado = EstadoCita.CANCELADO)).onSuccess {
                _effect.send(UiEffect.ShowSnackbar(AppMessage.Success(UiText.StringResource(R.string.warning_cita_cancelada))))
            }.onFailure {
                _effect.send(UiEffect.ShowSnackbar(AppMessage.Error(it.toUiText())))
            }
        }
    }
}