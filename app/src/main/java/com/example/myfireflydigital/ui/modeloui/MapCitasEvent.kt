package com.example.myfireflydigital.ui.modeloui

import com.example.myfireflydigital.domain.model.Cita

sealed interface MapCitasEvent {
    data object OnMapLoaded                     : MapCitasEvent
    data object OnMyLocation                   : MapCitasEvent
    data class OnSelectCita(val cita : Cita)    : MapCitasEvent
    data object OnClearRoute                    : MapCitasEvent
}