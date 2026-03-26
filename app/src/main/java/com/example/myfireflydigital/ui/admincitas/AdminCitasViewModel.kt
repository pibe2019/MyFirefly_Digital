package com.example.myfireflydigital.ui.admincitas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.query
import com.example.myfireflydigital.domain.exceptions.toUiText
import com.example.myfireflydigital.domain.model.AppMessage
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.PlaceLocation
import com.example.myfireflydigital.domain.model.result.EstadoCita
import com.example.myfireflydigital.domain.usecase.DeleteCitaUseCase
import com.example.myfireflydigital.domain.usecase.GetCitaByIdUseCase
import com.example.myfireflydigital.domain.usecase.GetCitasObserverUseCase
import com.example.myfireflydigital.domain.usecase.GetCurrentLocationUseCase
import com.example.myfireflydigital.domain.usecase.GetPlaceDetailsUseCase
import com.example.myfireflydigital.domain.usecase.GetPlaceSearchUseCase
import com.example.myfireflydigital.domain.usecase.ReverseGeocoding
import com.example.myfireflydigital.domain.usecase.UpsertCitaUseCase
import com.example.myfireflydigital.domain.util.UiText
import com.example.myfireflydigital.ui.core.util.cleanInput
import com.example.myfireflydigital.ui.modeloui.AdminCitasEvent
import com.example.myfireflydigital.ui.modeloui.AdminCitasUiState
import com.example.myfireflydigital.ui.modeloui.CitasUiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections.emptyList
import javax.inject.Inject

@HiltViewModel
class AdminCitasViewModel @Inject constructor(
    private val upsertCitaUseCase: UpsertCitaUseCase,
    private val deleteCitaUseCase: DeleteCitaUseCase,
    private val getCitasObserverUseCase: GetCitasObserverUseCase,
    private val getCitaByIdUseCase: GetCitaByIdUseCase,
    private val getPlaceSearchUseCase: GetPlaceSearchUseCase,
    private val getPlaceDetailsUseCase: GetPlaceDetailsUseCase,
    private val reverseGeocoding: ReverseGeocoding,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase//ubicacion nactual
) : ViewModel() {
    private val _addressQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val searchFlow = _addressQuery
        .flatMapLatest { query ->
            // Si el query es corto/vacío → emite emptyList() inmediatamente
            if (query.cleanInput().length <= 3) {
                flowOf(emptyList())
            } else {
                flow {
                    delay(400)
                    val result = getPlaceSearchUseCase(query)
                    result.onSuccess { listPredictions ->
                        emit(listPredictions)
                    }.onFailure { error ->
                        val mensaje = AppMessage.Error(error.toUiText())
                        _uiEffect.trySend(CitasUiEffect.ShowSnackbar(mensaje))
                        emit(emptyList())
                    }
                }
            }
        }
        //.map { result -> result.getOrElse { emptyList() } }//desembuelbe el Result<List<Prediction>> para -> List<Prediction> y lo pasa al "combine"
        .onStart { emit(emptyList()) }
    private val _uiEffect = Channel<CitasUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()
    private val _uiState = MutableStateFlow(AdminCitasUiState())
    val uiState: StateFlow<AdminCitasUiState> = combine(
        _uiState,
        getCitasObserverUseCase(),
        searchFlow
    ) { uiState, citasFromDb, predictions ->
        uiState.copy(citas = citasFromDb, isLoading = false, placePredictions = predictions, isLoadingSearchingPlace = if(predictions.isEmpty() || uiState.addressQuery.cleanInput().length<=3) { false } else { uiState.isLoadingSearchingPlace}) // si hay predicciones o consulta corta apaga isLoadingSearchingPlace
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminCitasUiState(isLoading = true)
    )

    /*para EL debounce - cancelar si el usuario vuelve a escribir antes del tiempo establecido*/
    var reverseGeocodeJob: Job? = null

    //STADOS DERIVADOS
    val citasObserver: StateFlow<List<Cita>> = getCitasObserverUseCase()
        //.onEach(::getCitasObserve)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onEvent(event: AdminCitasEvent) {
        when (event) {
            is AdminCitasEvent.OnUpsertCita -> upsertCita(event.cita)
            is AdminCitasEvent.OnDeleteCita -> deleteCita(event.cita)
            is AdminCitasEvent.OnSelectCita -> getCitasById(event.id)
            is AdminCitasEvent.OnLongPressCitaOpenSheet -> openSheetForEdit(event.id)
            AdminCitasEvent.OnOpenSheet -> {
                _addressQuery.value = ""
                _uiState.update { it.copy(citaSelectEnEdicion = Cita(id=0, titulo = "", direccion = "", fecha = "", hora="", latitud = 0.0, longitud = 0.0, estado = EstadoCita.NO_VISITADO),isSheetVisible = true, selectedLocation = null, addressQuery = "", isReverseGeocoding = false, isLocationManualAdjusted = false) }
            }
            is AdminCitasEvent.OnFormNew -> _uiState.update { it.copy(citaSelectEnEdicion = event.cita) }
            AdminCitasEvent.OnCloseSheet -> _uiState.update {
                Log.d("AdminCitasViewModel", "onEvent: ${it.citaSelectEnEdicion} - ${it.selectedLocation}")
                it.copy(isSheetVisible = false)
            }

            AdminCitasEvent.OnDismissError -> _uiState.update { it.copy(error = null) }
            AdminCitasEvent.OnDismissDetalle -> _uiState.update { it.copy(citaSelectEnEdicion = null) }//para una pantalla de detalle
            //AdminCitasEvent.OnCleanPredictions -> _uiState.update { it.copy(placePredictions = emptyList()) }
            //MAP PICKER
            AdminCitasEvent.OnOpenMapPicker -> {
                _addressQuery.value = ""
                _uiState.update {
                    it.copy(
                        isMapPikerVisible = true,
                        isSheetVisible = false,
                        isReverseGeocoding = false,
                        isLocationManualAdjusted = false,
                        locationBackup = it.selectedLocation,
                        addressQueryBackup = it.addressQuery
                    )
                }
                if (_uiState.value.selectedLocation == null) fetchCurrentLocationForMap()
            }
            AdminCitasEvent.OnCloseMapPicker -> {
                _addressQuery.value = ""
                _uiState.update { it.copy(isMapPikerVisible = false, isSheetVisible = true, selectedLocation = it.locationBackup, addressQuery = it.addressQueryBackup, locationBackup = null, addressQueryBackup = "") }
            }

            is AdminCitasEvent.OnConfirmMapLocation -> confirmMapLocation(event.lat,event.lng)//_uiState.update { it.copy(isMapPikerVisible = false) }

            is AdminCitasEvent.OnAddressQueryChanged -> addresQueryChanged(event.query)
            is AdminCitasEvent.OnPredictionSelected -> fetchPlaceDetails(event.placeId)
            is AdminCitasEvent.OnMapMarkerMoved -> onMarkerMoved(event.lat, event.lng)
        }
    }

    private fun getCitasById(id: Int) {
        Log.d("AdminCitasViewModel", "getCitasById: $id")
        viewModelScope.launch {
            getCitaByIdUseCase(id).onSuccess { cita ->
                Log.d("AdminCitasViewModel", "cita: $cita")
                _uiState.update { it.copy(citaSelectEnEdicion = cita) }
                _uiState.update { it.copy(isSheetVisible = true) }
            }.onFailure { error ->
                val mensaje = AppMessage.Error(error.toUiText())
                _uiEffect.send(CitasUiEffect.ShowSnackbar(mensaje))
            }
        }
    }

    //CARGA LA CITA Y HABRE EL SHEET
    private fun openSheetForEdit(id: Int) {
        viewModelScope.launch {
            getCitaByIdUseCase(id).onSuccess { cita ->
                val updateSelectLocation = cita?.let { cita ->
                    PlaceLocation(latitud = cita.latitud  , longitud = cita.longitud, address = cita.direccion)
                }
                _addressQuery.value = ""  // dispara el searchFlow, si no fuera  ""
                _uiState.update { it.copy(
                    citaSelectEnEdicion = cita,
                    isSheetVisible = true,
                    addressQuery = cita?.direccion ?: "",
                    selectedLocation = updateSelectLocation) }
            }.onFailure { error ->
                _uiEffect.send(CitasUiEffect.ShowSnackbar(AppMessage.Error(error.toUiText())))
                _uiState.update { it.copy(error= AppMessage.Error(error.toUiText())) }
            }
        }
    }

    //UPDATE - INSERT
    private fun upsertCita(cita: Cita) {
        viewModelScope.launch {
            upsertCitaUseCase(cita).onSuccess {
                _uiState.update { it.copy(isSheetVisible = false, citaSelectEnEdicion = null, addressQuery = "", selectedLocation = null) }
                val mensaje = AppMessage.Success(UiText.DynamicString("Cita guardada"))
                //_uiEffect.send(CitasUiEffect.DismissSheet)
                _uiEffect.send(CitasUiEffect.ShowSnackbar(mensaje))
            }.onFailure { error ->
                _uiState.update { it.copy(error = AppMessage.Error(error.toUiText())) }
                // Manejar error
            }
        }
    }

    private fun deleteCita(cita: Cita) {
        viewModelScope.launch {
            deleteCitaUseCase(cita).onSuccess {
                val mensaje = AppMessage.Success(UiText.DynamicString("Cita eliminada"))
                _uiEffect.send(CitasUiEffect.ShowSnackbar(mensaje))
            }.onFailure { error ->
                val mensaje = AppMessage.Error(error.toUiText())
                _uiEffect.send(CitasUiEffect.ShowSnackbar(mensaje))
            }
        }
    }

    /*
    * DEBOUNCE de 400ms
    * */
    private fun addresQueryChanged(query: String) {
        _addressQuery.value = query
        _uiState.update { it.copy(addressQuery = query, isLoadingSearchingPlace = query.cleanInput().length>3) }
    }

    private fun fetchPlaceDetails(placeId: String) {
        _addressQuery.value = ""
        viewModelScope.launch {
            _uiState.update {
                it.copy(placePredictions = emptyList(), isLoadingSearchingPlace = true)
            }
            getPlaceDetailsUseCase(placeId).onSuccess { placeLocation ->
                _uiState.update {
                    it.copy(
                        addressQuery = placeLocation.address,
                        selectedLocation = placeLocation,
                        isLoadingSearchingPlace = false,
                        isLocationManualAdjusted = false
                    )
                }
            }.onFailure { error ->
                val mensaje = AppMessage.Error(error.toUiText())
                _uiState.update { it.copy(isLoadingSearchingPlace = false) }
                _uiEffect.send(CitasUiEffect.ShowSnackbar(mensaje))
            }
        }
    }

    private fun confirmMapLocation(lat: Double, lng: Double) {
        reverseGeocodeJob?.cancel()
        //CIERRA EL PICKER Y ACTUALIZA COORDENADAS INMEDIATO
        _uiState.update { state ->
            val updateLocation = state.selectedLocation?.copy(latitud = lat, longitud = lng)
                ?: PlaceLocation(latitud = lat, longitud = lng, address = state.addressQuery)
            state.copy(
                selectedLocation = updateLocation,
                isReverseGeocoding = true,
                isLocationManualAdjusted = true,
                isMapPikerVisible = false, //CIERRA EL MAP-PICKER
                isSheetVisible = true,//REABRE EL SHEET
                locationBackup = null,
                addressQueryBackup = ""
            )
        }

        //REVERSE GEOCODING EN BACKGROUND- SHEET YA VISIBLE
        reverseGeocodeJob = viewModelScope.launch {
            reverseGeocoding(lat, lng).onSuccess { address ->
                Log.d("AdminCitasViewModel", "onMarketerMoved direccion nueva: $address")
                _addressQuery.value = ""
                _uiState.update {
                    it.copy(
                        addressQuery = address,
                        selectedLocation = it.selectedLocation?.copy(address = address),
                        isReverseGeocoding = false
                    )
                }
            }.onFailure { error ->
                val mensaje = AppMessage.Error(error.toUiText())
                _uiState.update { it.copy(addressQuery = "$lat, $lng", isReverseGeocoding = false) }
            }
        }
    }

    /*selectedLocation?: si nunca uso el autocomplete, entonces alli puede ser null*/
    private fun onMarkerMoved(lat: Double, lng: Double) {
        Log.d("AdminCitasViewModel", "onMarketerMoved: $lat, $lng")
        _uiState.update {
            val updatePlace =
                it.selectedLocation?.copy(latitud = lat, longitud = lng) ?: PlaceLocation(
                    lat,
                    lng,
                    it.addressQuery
                )
            it.copy(
                selectedLocation = updatePlace,
                isReverseGeocoding = true, // spinner en el picker
                isLocationManualAdjusted = true // badge "ajustado manualmente"
            )
        }
        //reverseGeocoding en paralelo
        reverseGeocodeJob?.cancel()
        reverseGeocodeJob = viewModelScope.launch {
            delay(600)//espera q el user suelte el pin
            reverseGeocoding(lat, lng).onSuccess { address ->

                _uiState.update {
                    Log.d("AdminCitasViewModel", "onMarketerMoved direccion nueva: address: $address - $it")
                    it.copy(
                        addressQuery = address,
                        selectedLocation = it.selectedLocation?.copy(address = address),
                        isReverseGeocoding = false
                    )
                }

            }.onFailure { error ->
                val mensaje = AppMessage.Error(error.toUiText())
                _uiState.update { it.copy(addressQuery = "$lat, $lng", isReverseGeocoding = false) }
                //val mensaje = AppMessage.Error(error.toUiText())
                //_uiEffect.trySend(CitasUiEffect.ShowSnackbar(mensaje))
            }
        }
    }

    //Mi ubicacion
    private fun fetchCurrentLocationForMap(){
        viewModelScope.launch {
            getCurrentLocationUseCase().onSuccess { latLong ->
                Log.d("AdminCitasViewModel", "fetchCurrentLocationForMap onSuccess: ${latLong}")
                if (_uiState.value.selectedLocation == null){
                    _uiState.update { it.copy(selectedLocation = PlaceLocation(
                        latitud = latLong.latitude,
                        longitud = latLong.longitude,
                        address = ""
                    ))}
                }
            }.onFailure {
                _uiEffect.send( CitasUiEffect.ShowSnackbar(AppMessage.Error(it.toUiText())))
                Log.d("AdminCitasViewModel", "fetchCurrentLocationForMap onFailure: ${it.message}")
            }
        }
    }

}