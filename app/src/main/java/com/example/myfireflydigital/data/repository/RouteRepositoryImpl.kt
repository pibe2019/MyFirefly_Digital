package com.example.myfireflydigital.data.repository

import android.util.Log
import com.example.myfireflydigital.data.mapper.toDomain
import com.example.myfireflydigital.data.remote.apiservices.RouteApiService
import com.example.myfireflydigital.data.remote.dto.LatLngDto
import com.example.myfireflydigital.data.remote.dto.LocationWaypointDto
import com.example.myfireflydigital.data.remote.dto.RouteResquestDto
import com.example.myfireflydigital.data.remote.dto.WaypointDto
import com.example.myfireflydigital.di.IoDispatcher
import com.example.myfireflydigital.domain.exceptions.RouteNotFoundException
import com.example.myfireflydigital.domain.exceptions.RouteServiceException
import com.example.myfireflydigital.domain.model.RouteResult
import com.example.myfireflydigital.domain.repository.RouteRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(private val routeApiService: RouteApiService, @IoDispatcher private val ioDispatcher: CoroutineDispatcher) : RouteRepository {

    override suspend fun getRoute(origin: LatLng,destination: LatLng): Result<RouteResult> =
        withContext(ioDispatcher){
            runCatching {
                val request = RouteResquestDto(
                    origin = WaypointDto(
                        location = LocationWaypointDto(
                            latLng = LatLngDto(
                                latitude = origin.latitude,
                                longitude = origin.longitude
                            )
                        )
                    ),
                    destination = WaypointDto(
                        location = LocationWaypointDto(
                            latLng = LatLngDto(
                                latitude = destination.latitude,
                                longitude = destination.longitude
                            )
                        )
                    )
                )
                val response = routeApiService.computeRoutes(request)
                val routeDto = response.routes.firstOrNull() ?: throw RouteNotFoundException()
                Log.d("TAG", "getRoute: ${routeDto.toDomain()}")
                routeDto.toDomain()
            }.recoverCatching { throwable ->
                Log.d("TAG", "getRoute 2: ${throwable.message}")
                if (throwable is CancellationException) throw throwable
                if (throwable is RouteNotFoundException) throw throwable
                throw RouteServiceException("${throwable.message}")
            }
        }
}