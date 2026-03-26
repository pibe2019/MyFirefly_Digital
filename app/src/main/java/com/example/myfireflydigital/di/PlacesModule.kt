package com.example.myfireflydigital.di

import android.content.Context
import android.location.Geocoder
import com.example.myfireflydigital.BuildConfig
import com.example.myfireflydigital.data.repository.PlacesRepositoryImpl
import com.example.myfireflydigital.domain.repository.PlacesRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {

    @Provides
    @Singleton
    fun providePlacesClient (@ApplicationContext context: Context): PlacesClient {
        if (!Places.isInitialized()){
            Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.MAPS_API_KEY)
        }
        return Places.createClient(context)
    }

    @Provides
    @Singleton
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder = Geocoder(context, Locale.getDefault())

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    /*@Provides
    @Singleton
    fun providePlacesRepository(placesClient: PlacesClient) : PlacesRepository{
        return PlacesRepositoryImpl(placesClient)
    }*/
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PlacesBindModule{

    @Singleton
    @Binds
    abstract fun bindPlacesRepository(impl: PlacesRepositoryImpl) : PlacesRepository
}