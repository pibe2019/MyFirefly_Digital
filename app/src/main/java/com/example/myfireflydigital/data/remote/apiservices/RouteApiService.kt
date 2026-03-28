package com.example.myfireflydigital.data.remote.apiservices
import com.example.myfireflydigital.data.remote.dto.RouteResponseDto
import com.example.myfireflydigital.data.remote.dto.RouteResquestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface RouteApiService {
    @POST("directions/v2:computeRoutes") //ROUTE API NUEVA
    @Headers("X-Goog-FieldMask: routes.polyline.encodedPolyline,routes.distanceMeters,routes.duration")
    suspend fun computeRoutes(@Body request: RouteResquestDto): RouteResponseDto
}