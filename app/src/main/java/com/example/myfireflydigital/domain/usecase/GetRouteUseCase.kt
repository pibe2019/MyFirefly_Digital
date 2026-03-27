package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.repository.RouteRepository
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GetRouteUseCase @Inject constructor(private val routeRepository: RouteRepository) {
    suspend operator fun invoke(origin: LatLng, destination: LatLng): Result<List<LatLng>> = routeRepository.getRoute(origin, destination)
}