package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.repository.PlacesRepository
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(private val placesRepository: PlacesRepository) {
    suspend operator fun invoke() : Result<LatLng> = placesRepository.getCurrentLocation()
}