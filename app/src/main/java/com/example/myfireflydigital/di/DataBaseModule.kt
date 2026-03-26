package com.example.myfireflydigital.di

import android.content.Context
import androidx.room.Room
import com.example.myfireflydigital.data.local.dao.CitaDao
import com.example.myfireflydigital.data.local.database.AppDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    private const val CITA_MEDICA_DATABASE = "Cita_Medica_DB"

    @Singleton
    @Provides
    fun providesRoom(@ApplicationContext context: Context) : AppDataBase = Room.databaseBuilder(context,AppDataBase::class.java, CITA_MEDICA_DATABASE).build()

    @Singleton
    @Provides
    fun providesCitaDao(db: AppDataBase) : CitaDao = db.citaDao()

}