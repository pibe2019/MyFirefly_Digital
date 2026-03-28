package com.example.myfireflydigital.ui.mapcitas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfireflydigital.R
import com.example.myfireflydigital.domain.exceptions.toUiText
import com.example.myfireflydigital.domain.model.AppMessage
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.usecase.GetCitasObserverUseCase
import com.example.myfireflydigital.domain.usecase.GetCurrentLocationUseCase
import com.example.myfireflydigital.domain.usecase.GetRouteUseCase
import com.example.myfireflydigital.domain.util.UiText
import com.example.myfireflydigital.ui.modeloui.AdminCitasUiState
import com.example.myfireflydigital.ui.modeloui.MapCitasEvent
import com.example.myfireflydigital.ui.modeloui.MapUiState
import com.example.myfireflydigital.ui.modeloui.RouteInfo
import com.example.myfireflydigital.ui.modeloui.UiEffect
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import javax.inject.Inject

@HiltViewModel
class MapCitasViewModel @Inject constructor(
    private val getCurrenLocationUseCase: GetCurrentLocationUseCase,
    private val getCitasObserverUseCase: GetCitasObserverUseCase,
    private val getRouteUseCase: GetRouteUseCase
) : ViewModel() {
    private val _uiMapState = MutableStateFlow(MapUiState())
    //val uiMapState : StateFlow<MapUiState> = _uiMapState.asStateFlow()

    val uiMapState: StateFlow<MapUiState> = combine(
        _uiMapState,
        getCitasObserverUseCase()
    ) { uiState, citasFromDb ->
        uiState.copy(citas = citasFromDb)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState(isLoadingCitas = true, isLoadingMap = true)
    )
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var routerJob: Job? = null //DEBOUNCE

    fun OnEvent(event: MapCitasEvent) {
        when (event) {
            MapCitasEvent.OnMapLoaded -> _uiMapState.update { it.copy(isLoadingMap = false) }
            MapCitasEvent.OnMyLocation -> OnMyLocation()
            is MapCitasEvent.OnSelectCita -> onSelectCita(event.cita)
            MapCitasEvent.OnClearRoute -> onClearRoute()
        }
    }

    private fun OnMyLocation() {
        viewModelScope.launch {
            //_uiMapState.update { it.copy(isLoading = true) }
            getCurrenLocationUseCase().onSuccess { latLng ->
                _uiMapState.update {
                    it.copy(
                        userLocation = latLng,
                        //isLoading = false,
                        properties = it.properties.copy(isMyLocationEnabled = true)
                    )
                }
            }.onFailure { error ->
                //_uiMapState.update { it.copy(isLoading = false) }
                _effect.send(UiEffect.ShowSnackbar(AppMessage.Error(error.toUiText())))
            }
        }
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
        viewModelScope.launch {
            _uiMapState.update {
                it.copy(
                    isLoadingRouteUbi = true,
                    citaSelecId = cita.id,
                    routeInfo = null
                )
            }
            getRouteUseCase(userLocation, LatLng(cita.latitud, cita.longitud))
                .onSuccess { routeInfo ->
                    Log.d("MapCitasViewModel", "onSelectCita 2: ${routeInfo}")
                    _uiMapState.update { it.copy(isLoadingRouteUbi = false, routeInfo = RouteInfo(routeInfo.points, routeInfo.distance, routeInfo.duration)) }
                }.onFailure {
                    Log.d("MapCitasViewModel", "onSelectCita 3: ${it}")
                    _uiMapState.update { it.copy(isLoadingRouteUbi = false, citaSelecId = null) }
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
}