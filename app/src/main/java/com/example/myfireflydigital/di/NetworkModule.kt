package com.example.myfireflydigital.di

import com.example.myfireflydigital.data.remote.apiservices.RouteApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.myfireflydigital.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Duration
import javax.inject.Singleton
/*
ROUTE API NUEVA
*/

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://routes.googleapis.com/"

    @Singleton
    @Provides
    fun provideOkHttpClient() : OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(20))
            .readTimeout(Duration.ofSeconds(20))
            .writeTimeout(Duration.ofSeconds(20))
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().addHeader("X-Goog-Api-Key", BuildConfig.MAPS_API_KEY).build()
                chain.proceed(request)
            }
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(pkHttpClient: OkHttpClient, json: Json): Retrofit{
        return Retrofit.Builder().baseUrl(BASE_URL).client(pkHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
    }

    @Singleton
    @Provides
    fun provideJson() : Json {
        return Json{
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    @Singleton
    @Provides
    fun provideRutasApiService(retrofit: Retrofit) : RouteApiService {
        return retrofit.create(RouteApiService::class.java)
    }

}