package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.model.PlaceLocation
import com.example.myfireflydigital.domain.repository.PlacesRepository
import javax.inject.Inject

class GetPlaceDetailsUseCase @Inject constructor(private val placesRepository: PlacesRepository){
    suspend operator fun invoke(placeId: String) : Result<PlaceLocation> = placesRepository.getPlaceDetails(placeId)
}
