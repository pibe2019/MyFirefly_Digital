package com.example.myfireflydigital.data.repository

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.myfireflydigital.data.source.LocationProvider
import com.example.myfireflydigital.di.IoDispatcher
import com.example.myfireflydigital.domain.exceptions.GeoLocationDisableException
import com.example.myfireflydigital.domain.exceptions.GeoLocationPermissionDeniedException
import com.example.myfireflydigital.domain.exceptions.GeoLocationUnknownException
import com.example.myfireflydigital.domain.exceptions.GeocoderException
import com.example.myfireflydigital.domain.exceptions.GeocoderNotAvailableException
import com.example.myfireflydigital.domain.exceptions.PlaceServiceException
import com.example.myfireflydigital.domain.model.PlaceLocation
import com.example.myfireflydigital.domain.model.PlacePrediction
import com.example.myfireflydigital.domain.repository.PlacesRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

@Singleton
class PlacesRepositoryImpl @Inject constructor(
    private val placesClient: PlacesClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val geocoder: Geocoder,
    private val locationProvider: LocationProvider
) : PlacesRepository {
    private var sesionToken: AutocompleteSessionToken? = null
    override suspend fun getPlaceSearchAutoCompletePredictions(query: String): Result<List<PlacePrediction>> {
        return runCatching {
            if (sesionToken == null) sesionToken = AutocompleteSessionToken.newInstance()
            val request =
                FindAutocompletePredictionsRequest.builder()//relacionado al costo de la peticion $
                    .setQuery(query)
                    .setSessionToken(sesionToken)
                    .setCountries("PE")
                    .build()
            val response = placesClient.findAutocompletePredictions(request).await()
            response.autocompletePredictions.map { prediction ->
                PlacePrediction(
                    placeId = prediction.placeId,
                    primaryText = prediction.getPrimaryText(null).toString(),
                    secondaryText = prediction.getSecondaryText(null).toString(),
                    fullText = prediction.getFullText(null).toString()
                )
            }
        }.recoverCatching { throwable ->
            if (throwable is CancellationException) throw throwable
            throw PlaceServiceException("${throwable.message}")
        }
    }

    override suspend fun getPlaceDetails(placeId: String): Result<PlaceLocation> {
        return runCatching {
            sesionToken = null
            val fields = listOf(
                Place.Field.LOCATION,
                Place.Field.FORMATTED_ADDRESS
            ) // relaciondo al costo $ de la peticion x eso solo pide 2 cosas
            val request = FetchPlaceRequest.newInstance(placeId, fields)
            val response =
                placesClient.fetchPlace(request).await()// gracias al .await() ahora es una corutina
            val place = response.place

            PlaceLocation(
                latitud = place.location?.latitude ?: 0.0,
                longitud = place.location?.longitude ?: 0.0,
                address = place.formattedAddress ?: ""
            )
        }.recoverCatching { throwable ->
            if (throwable is CancellationException) throw throwable
            throw PlaceServiceException("${throwable.message}")
        }
    }

    override suspend fun reverseGeocode(
        lat: Double,
        lng: Double
    ): Result<String> {
        if (!Geocoder.isPresent()) return Result.failure(GeocoderNotAvailableException())
        return runCatching {
            withContext(ioDispatcher) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    addresses?.firstOrNull()?.getAddressLine(0) ?: "$lat, $lng"
                } else {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: List<Address?>) {
                                val address =
                                    addresses.firstOrNull()?.getAddressLine(0) ?: "$lat, $lng"
                                continuation.resume(address)
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume("$lat, $lng")
                            }
                        })
                    }
                }
            }
        }.recoverCatching { throwable ->
            if (throwable is CancellationException) throw throwable
            throw GeocoderException("${throwable.message}")
        }
    }

    /* OBTENEMOS LA UBICACION - Manejamos el riesgo con SecurityException en recoverCatching */
    @SuppressLint("MissingPermission")//permisos de ubicacion
    override suspend fun getCurrentLocation(): Result<LatLng> {
        return runCatching {
            locationProvider.fetchCurrentLocation()
        }.recoverCatching {
            when(it){
                is CancellationException -> throw it
                is SecurityException -> throw GeoLocationPermissionDeniedException()
                is GeoLocationDisableException -> throw GeoLocationDisableException()
                is GeoLocationUnknownException -> throw GeoLocationUnknownException()
                else -> throw GeoLocationUnknownException("${it.message}")
            }
        }
    }


}
