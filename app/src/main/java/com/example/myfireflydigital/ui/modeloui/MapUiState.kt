package com.example.myfireflydigital.ui.modeloui

import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.RouteResult
import com.google.android.gms.maps.model.LatLng

data class MapUiState (
    val citas: List<Cita> = emptyList(),
    val userLocation: LatLng? = null,
    val isCitasLoaded: Boolean = false, //estan cargadas las citas?
    val isMapLoaded: Boolean = false,    //esta cargado el map?
    val locationUpdateTick: Int = 0,
    //val uiSettings: MapUiSettings = MapUiSettings(zoomControlsEnabled = false),
    //RUTA
    val citaSelecId: Int? = null, //ruta destino
    val isLoadingRouteUbi: Boolean = false,//compartir con ubi
    val routeInfo: RouteResult? = null, //distancia, duraciony polyline
    //val error: AppMessage? = null
)
data class RouteInfo(
    val routePoints: List<LatLng>,
    val distance: String,
    val duration: String
)