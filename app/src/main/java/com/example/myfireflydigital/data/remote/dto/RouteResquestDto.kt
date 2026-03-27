package com.example.myfireflydigital.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RouteResquestDto(
    val origin: WaypointDto,
    val destination: WaypointDto,
    val travelMode: String = "DRIVE",
    val routingPreference: String = "TRAFFIC_AWARE_OPTIMAL",/*considera tráfico en tiempo real para la mejor ruta*/
    val polylineQuality: String = "HIGH_QUALITY", /*más puntos en el polyline = curvas más suaves en el mapa*/
    //val languageCode: String = "es-ES" // recive en español las instrucciones
    //val computeAlternativeRoutes: Boolean = false // me da opciones de rutas, de lo contrario solo me da una(mejor opcion)- SI NO LO PIDES, lo entiende por defecto como false
)
@Serializable
data class WaypointDto(val location: LocationWaypointDto)
@Serializable
data class LocationWaypointDto(val latLng: LatLngDto)
@Serializable
data class LatLngDto(val latitude: Double, val longitude: Double)
