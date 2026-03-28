package com.example.myfireflydigital.domain.model
import com.google.android.gms.maps.model.LatLng

data class RouteResult(
    val points : List<LatLng>,//polyline decodificada
    val distance : String,
    val duration : String
)