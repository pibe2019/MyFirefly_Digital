package com.example.myfireflydigital.ui.mapcitas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfireflydigital.domain.exceptions.toUiText
import com.example.myfireflydigital.domain.model.AppMessage
import com.example.myfireflydigital.domain.usecase.GetCitasObserverUseCase
import com.example.myfireflydigital.domain.usecase.GetCurrentLocationUseCase
import com.example.myfireflydigital.ui.modeloui.AdminCitasUiState
import com.example.myfireflydigital.ui.modeloui.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import javax.inject.Inject

@HiltViewModel
class MapCitasViewModel @Inject constructor(private val getCurrenLocationUseCase: GetCurrentLocationUseCase, private val getCitasObserverUseCase: GetCitasObserverUseCase): ViewModel() {
    private val _uiMapState = MutableStateFlow(MapUiState())
    //val uiMapState : StateFlow<MapUiState> = _uiMapState.asStateFlow()

    val uiMapState: StateFlow<MapUiState> = combine(
        _uiMapState,
        getCitasObserverUseCase()
    ) { uiState, citasFromDb ->
        uiState.copy(citas = citasFromDb, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState(isLoading = true)
    )



    fun onPermissionGranted(){
        viewModelScope.launch {
            _uiMapState.update { it.copy(isLoading = true) }
            getCurrenLocationUseCase().onSuccess { latLng ->
                _uiMapState.update { it.copy(userLocation = latLng, isLoading = false, properties = it.properties.copy(isMyLocationEnabled = true)) }
            }.onFailure { error ->
                _uiMapState.update { it.copy(error = AppMessage.Error(error.toUiText()), isLoading = false) }
            }
        }
    }

}