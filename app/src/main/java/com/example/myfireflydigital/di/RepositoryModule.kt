package com.example.myfireflydigital.di

import com.example.myfireflydigital.data.local.dao.CitaDao
import com.example.myfireflydigital.data.repository.CitaRepositoryImpl
import com.example.myfireflydigital.domain.repository.CitaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun providerCitaRepository(citaDaoApi: CitaDao) : CitaRepository = CitaRepositoryImpl(citaDaoApi)

}