package com.example.myfireflydigital.di

import com.example.myfireflydigital.data.local.dao.CitaDao
import com.example.myfireflydigital.data.remote.apiservices.RouteApiService
import com.example.myfireflydigital.data.repository.CitaRepositoryImpl
import com.example.myfireflydigital.data.repository.PlacesRepositoryImpl
import com.example.myfireflydigital.data.repository.RouteRepositoryImpl
import com.example.myfireflydigital.domain.repository.CitaRepository
import com.example.myfireflydigital.domain.repository.PlacesRepository
import com.example.myfireflydigital.domain.repository.RouteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindModule {

    @Singleton
    @Binds
    abstract fun bindCitaRepository(impl: CitaRepositoryImpl) : CitaRepository

    @Singleton
    @Binds
    abstract fun bindPlacesRepository(impl: PlacesRepositoryImpl) : PlacesRepository

    @Singleton
    @Binds
    abstract fun bindRouteRepository(impl: RouteRepositoryImpl) : RouteRepository
}

