package com.example.myfireflydigital.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.myfireflydigital.di.IoDispatcher
import com.example.myfireflydigital.domain.exceptions.GeoLocationUnknownException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationProvider @Inject constructor(private val fusedLocationProviderClient: FusedLocationProviderClient, @ApplicationContext private val context: Context, @IoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): LatLng = withContext(ioDispatcher){
        //Verifica si GPS está listo via SettingsClient
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).build()
        val settingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
        // lanza ResolvableApiException si GPS apagado
        LocationServices.getSettingsClient(context).checkLocationSettings(settingsRequest).await()

        /*val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if(!isGpsEnable && !isNetworkEnable) {
            throw GeoLocationDisableException()
        }*/

        val location = fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).await()

        location?.let { LatLng(location.latitude, location.longitude) } ?: throw GeoLocationUnknownException("no se obtuvo la ubicacion")
    }

    @SuppressLint("MissingPermission")
    fun locationUpdates(intervalMS: Long = 4000): Flow<LatLng> = callbackFlow{
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMS)
            .setMinUpdateIntervalMillis(2000)   // no mas rapido q cada 2 segundos
            .setMinUpdateDistanceMeters(5f)     // solo si se movio >= 5 metros
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(LatLng(it.latitude, it.longitude)) }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper()).addOnFailureListener { e -> close(e) }
        awaitClose { fusedLocationProviderClient.removeLocationUpdates(callback) }
        }.flowOn(ioDispatcher)
}
