package com.example.myfireflydigital.ui.modeloui

import com.example.myfireflydigital.domain.model.AppMessage
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.PlaceLocation
import com.example.myfireflydigital.domain.model.PlacePrediction
import com.example.myfireflydigital.domain.model.result.EstadoCita

data class AdminCitasUiState(
    val citas: List<Cita> = emptyList(),
    val isLoading: Boolean = false,
    val error: AppMessage? = null,
    //SHEET CREAR-EDITAR
    val isSheetVisible: Boolean = false, // Control centralizado del Sheet
    val citaSelectEnEdicion: Cita? = null,//edi-detalle- o nueva
    //PLACE AUTOCOMPLETAR
    val addressQuery: String="",
    val placePredictions: List<PlacePrediction> = emptyList(),
    val isLoadingSearchingPlace: Boolean = false,//loading del autocompletado y centrar el mapa
    //UBICACION CONFIRMADA
    val selectedLocation: PlaceLocation? = null, // lugar confirmado con la data final, ya sea al aceptar algun resultado de prediccion o al mover el marker
    val isReverseGeocoding: Boolean = false, //campo de direccion -- EN PRUEBA
    val isLocationManualAdjusted: Boolean = false, //bage "ajustado manualmente" -- EN PRUEBA
    //PANTALLA DE MAPS(SOBRE EL SHEET)
    val isMapPikerVisible : Boolean = false,
    //BACKUP DE UBICACION
    val locationBackup: PlaceLocation? = null,
    val addressQueryBackup: String = ""
)