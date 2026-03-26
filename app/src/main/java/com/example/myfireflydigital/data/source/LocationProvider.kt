package com.example.myfireflydigital.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import com.example.myfireflydigital.domain.exceptions.GeoLocationDisableException
import com.example.myfireflydigital.domain.exceptions.GeoLocationUnknownException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationProvider @Inject constructor(private val fusedLocationProviderClient: FusedLocationProviderClient, @ApplicationContext private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): LatLng{
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if(!isGpsEnable && !isNetworkEnable) {
            throw GeoLocationDisableException()
        }

        val location = fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).await()

        return location?.let { LatLng(location.latitude, location.longitude) } ?: throw GeoLocationUnknownException("no se obtuvo la ubicacion")
    }
}
