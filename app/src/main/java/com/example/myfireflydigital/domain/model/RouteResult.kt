package com.example.myfireflydigital.domain.model
import com.google.android.gms.maps.model.LatLng

data class Geolocation(val lat: Double, val lng: Double)
data class RouteResult(
    val routePoints : List<LatLng>,//polyline decodificada
    val distance : String,
    val duration : String
)