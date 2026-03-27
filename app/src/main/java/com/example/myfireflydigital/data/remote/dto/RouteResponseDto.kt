package com.example.myfireflydigital.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
class RouteResponseDto(val routes: List<RouteDto> = emptyList())

@Serializable
class RouteDto(
    val polyline: PolylineDto? = null,
    val distanceMeters: Int = 0,
    val duration: String = ""
)
 /*string comprimido con el algoritmo de google encode Polyline*/
@Serializable
class PolylineDto(val encodedPolyline: String = "")