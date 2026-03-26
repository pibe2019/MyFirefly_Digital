package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.repository.PlacesRepository
import javax.inject.Inject

class ReverseGeocoding @Inject constructor(private val placesRepository: PlacesRepository) {
    suspend operator fun invoke(lat: Double, lng: Double): Result<String> = placesRepository.reverseGeocode(lat, lng)
}