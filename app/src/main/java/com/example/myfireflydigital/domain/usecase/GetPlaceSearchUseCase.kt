package com.example.myfireflydigital.domain.usecase

import com.example.myfireflydigital.domain.model.PlacePrediction
import com.example.myfireflydigital.domain.repository.PlacesRepository
import javax.inject.Inject

class GetPlaceSearchUseCase @Inject constructor(private val placesRepository: PlacesRepository) {
    suspend operator fun invoke(query: String) : Result<List<PlacePrediction>>  = if(query.isBlank()) Result.success(emptyList())
        else placesRepository.getPlaceSearchAutoCompletePredictions(query)
}
