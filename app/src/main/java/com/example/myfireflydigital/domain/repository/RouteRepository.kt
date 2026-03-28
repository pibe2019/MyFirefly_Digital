package com.example.myfireflydigital.domain.repository

import com.example.myfireflydigital.domain.model.RouteResult
import com.google.android.gms.maps.model.LatLng

interface RouteRepository {
    suspend fun getRoute(origin: LatLng, destination: LatLng): Result<RouteResult>
}