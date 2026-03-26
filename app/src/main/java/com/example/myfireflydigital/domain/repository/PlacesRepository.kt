package com.example.myfireflydigital.domain.repository

import com.example.myfireflydigital.domain.model.PlaceLocation
import com.example.myfireflydigital.domain.model.PlacePrediction
import com.google.android.gms.maps.model.LatLng

interface PlacesRepository {
    /*devuelve sugerencias de busqueda*/
    suspend fun getPlaceSearchAutoCompletePredictions(query: String): Result<List<PlacePrediction>>//NEW API PLACES
    suspend fun getPlaceDetails(placeId: String): Result<PlaceLocation> //NEW API PLACES
    suspend fun reverseGeocode(lat: Double, lng: Double): Result<String> //GEOCODING
    suspend fun getCurrentLocation(): Result<LatLng>
}