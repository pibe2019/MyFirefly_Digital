package com.example.myfireflydigital.data.repository

import com.example.myfireflydigital.data.remote.apiservices.RouteApiService
import com.example.myfireflydigital.data.remote.dto.LatLngDto
import com.example.myfireflydigital.data.remote.dto.LocationWaypointDto
import com.example.myfireflydigital.data.remote.dto.RouteResquestDto
import com.example.myfireflydigital.data.remote.dto.WaypointDto
import com.example.myfireflydigital.di.IoDispatcher
import com.example.myfireflydigital.domain.exceptions.RouteNotFoundException
import com.example.myfireflydigital.domain.exceptions.RouteServiceException
import com.example.myfireflydigital.domain.repository.RouteRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(private val routeApiService: RouteApiService, @IoDispatcher private val ioDispatcher: CoroutineDispatcher) : RouteRepository {

    override suspend fun getRoute(origin: LatLng,destination: LatLng): Result<List<LatLng>> =
        withContext(ioDispatcher){
            runCatching {
                val request = RouteResquestDto(
                    origin      = WaypointDto(location = LocationWaypointDto(latLng = LatLngDto(latitude = origin.latitude,longitude= origin.longitude))),
                    destination = WaypointDto(location = LocationWaypointDto(latLng = LatLngDto(latitude = destination.latitude,longitude= destination.longitude)))
                )
                val response = routeApiService.computeRoutes(request)
                val encodedPolyline = response.routes.firstOrNull()?.polyline?.encodedPolyline ?: throw RouteNotFoundException()
                PolyUtil.decode(encodedPolyline)
            }.recoverCatching { throwable ->
                if (throwable is CancellationException) throw throwable
                if (throwable is RouteNotFoundException) throw throwable
                throw RouteServiceException("${throwable.message}")
            }
        }
}