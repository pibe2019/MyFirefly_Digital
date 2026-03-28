package com.example.myfireflydigital.ui.detallecita

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.usecase.GetCitaByIdUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetalleCitaViewModel.Factory::class)
class DetalleCitaViewModel @AssistedInject constructor(private val getCitaByIdUseCase: GetCitaByIdUseCase, @Assisted val idCita: Int) : ViewModel() {
    @AssistedFactory
    interface Factory{
        fun create(idCita: Int): DetalleCitaViewModel
    }
    private val _cita = MutableStateFlow<Cita?>(null)
    val cita: StateFlow<Cita?> = _cita.asStateFlow()

    init {
        viewModelScope.launch {
            getCitaByIdUseCase(idCita).onSuccess { cita ->
                _cita.value = cita
            }
        }
    }
    //val cita = getCitaByIdUseCase(idCita).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}