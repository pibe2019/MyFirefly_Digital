package com.example.myfireflydigital.ui.modeloui

import com.example.myfireflydigital.domain.model.Cita

sealed interface AdminCitasEvent {
    /*CRUD CITAS*/
    data class OnUpsertCita(val cita: Cita) : AdminCitasEvent
    data class  OnDeleteCita(val cita: Cita)  : AdminCitasEvent //detalle
    data class OnLongPressCitaOpenSheet(val id: Int)  : AdminCitasEvent //editar
    data class  OnSelectCita(val id: Int)     : AdminCitasEvent
    /* SHEET */
    data object OnOpenSheet                   : AdminCitasEvent
    data object OnCloseSheet                  : AdminCitasEvent
    data object OnDismissError                : AdminCitasEvent
    data object OnDismissDetalle              : AdminCitasEvent
    /*PLACE AUTOCOMPLETADO*/
    data class OnAddressQueryChanged(val query: String) : AdminCitasEvent//api place
    data class OnPredictionSelected(val placeId: String) : AdminCitasEvent//api place
    data class OnMapMarkerMoved(val lat: Double, val lng: Double) : AdminCitasEvent//api geocoding
    //data object OnCleanPredictions : AdminCitasEvent //limpio las predicciones al cerrar el DROPDOW -FALTA UTILIZAR
    //MAP PICKER
    data object OnOpenMapPicker                 : AdminCitasEvent
    data object OnCloseMapPicker                : AdminCitasEvent
    data class OnConfirmMapLocation(val lat: Double, val lng: Double) : AdminCitasEvent //geocoding
    //formulario nuevo
    data class OnFormNew(val cita: Cita) : AdminCitasEvent
}