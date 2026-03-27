package com.example.myfireflydigital.domain.repository

import com.google.android.gms.maps.model.LatLng

interface RouteRepository {
    suspend fun getRoute(origin: LatLng, destination: LatLng): Result<List<LatLng>>
}