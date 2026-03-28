package com.example.myfireflydigital.ui.modeloui

import com.example.myfireflydigital.domain.model.Cita
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType

data class MapUiState (
    val citas: List<Cita> = emptyList(),
    val userLocation: LatLng? = null,
    val isLoadingCitas: Boolean = false,
    val isLoadingMap: Boolean = false,//map sta cargado?
    val properties: MapProperties = MapProperties(isMyLocationEnabled = false, mapType = MapType.NORMAL),
    //val uiSettings: MapUiSettings = MapUiSettings(zoomControlsEnabled = false),
    //RUTA
    val citaSelecId: Int? = null, //ruta destino
    val isLoadingRouteUbi: Boolean = false,//compartir con ubi
    val routeInfo: RouteInfo? = null, //distancia, duraciony polyline
    //val error: AppMessage? = null
)
data class RouteInfo(
    val routePoints: List<LatLng>,
    val distance: String,
    val duration: String
)