package com.example.myfireflydigital.ui.modeloui

import com.example.myfireflydigital.domain.model.AppMessage
import com.example.myfireflydigital.domain.model.Cita
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings

data class MapUiState (
    val citas: List<Cita> = emptyList(),
    val userLocation: LatLng? = null,
    val isLoading: Boolean = false,
    val isMapLoaded: Boolean = false,//map sta cargado?
    val properties: MapProperties = MapProperties(isMyLocationEnabled = false, mapType = MapType.NORMAL),
    val uiSettings: MapUiSettings = MapUiSettings(zoomControlsEnabled = false),
    val error: AppMessage? = null
)