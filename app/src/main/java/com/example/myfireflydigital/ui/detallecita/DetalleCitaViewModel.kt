package com.example.myfireflydigital.ui.detallecita

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfireflydigital.R
import com.example.myfireflydigital.domain.exceptions.toUiText
import com.example.myfireflydigital.domain.model.AppMessage
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.result.EstadoCita
import com.example.myfireflydigital.domain.usecase.GetCitaByIdUseCase
import com.example.myfireflydigital.domain.usecase.UpsertCitaUseCase
import com.example.myfireflydigital.domain.util.UiText
import com.example.myfireflydigital.ui.modeloui.UiEffect
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetalleCitaViewModel.Factory::class)
class DetalleCitaViewModel @AssistedInject constructor(
    private val getCitaByIdUseCase: GetCitaByIdUseCase,
    private val upsertCitaUseCase: UpsertCitaUseCase,
    @Assisted val idCita: Int
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(idCita: Int): DetalleCitaViewModel
    }
    //private val _cita = MutableStateFlow<Cita?>(null)
    //val cita2: StateFlow<Cita?> = _cita.asStateFlow()
    private val _uiEffect = Channel<UiEffect>(Channel.CONFLATED)//SOLO GUARDA EL ULTIMO AVISO
    val uiEffect = _uiEffect.receiveAsFlow()
    val cita: StateFlow<Cita?> = flow{ emit(getCitaByIdUseCase(idCita)) }
        .onEach { result ->
            result
                .onSuccess {cita ->
                    if (cita == null) _uiEffect.send(UiEffect.ShowSnackbar(AppMessage.Success(UiText.DynamicString("Cita no encontrada"))))
                }
                .onFailure {error -> _uiEffect.send(UiEffect.ShowSnackbar(AppMessage.Error(error.toUiText())))}
        }.map { cita -> cita.getOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        //.launchIn(viewModelScope)


    fun onEstadoChanged(estado: EstadoCita){
        //_cita.update { it?.copy(estado = estado) }
        viewModelScope.launch {
            val citaEstadoUpdate = cita.value?.copy(estado = estado) ?: return@launch
            //val citaEstadoUpdate = citaActual
            upsertCitaUseCase(citaEstadoUpdate).onSuccess {
                _uiEffect.send(UiEffect.ShowSnackbar(AppMessage.Success(UiText.StringResource(R.string.estado_actualizado))))
            }.onFailure { error ->
                _uiEffect.send(UiEffect.ShowSnackbar(AppMessage.Error(error.toUiText())))
            }
        }
    }
}