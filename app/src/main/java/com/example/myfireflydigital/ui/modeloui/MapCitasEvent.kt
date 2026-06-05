package com.example.myfireflydigital.ui.modeloui

import androidx.activity.result.IntentSenderRequest
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.result.EstadoCita

sealed interface MapCitasEvent {
    data object OnMapLoaded                     : MapCitasEvent
    data object OnMyLocation                   : MapCitasEvent
    data class OnSelectCita(val cita : Cita)    : MapCitasEvent
    data object OnClearRoute                    : MapCitasEvent
    data class OnCancelarCita(val citaId: Int) : MapCitasEvent
    data object OnMyLocationButtonClick : MapCitasEvent
}