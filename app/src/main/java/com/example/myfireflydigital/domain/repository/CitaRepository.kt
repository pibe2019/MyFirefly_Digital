package com.example.myfireflydigital.domain.repository

import com.example.myfireflydigital.domain.model.Cita
import kotlinx.coroutines.flow.Flow

interface CitaRepository {
    fun getCitasObserve(): Flow<List<Cita>>
    suspend fun getCitaById(id: Int): Result<Cita?>
    suspend fun upsertCita(cita: Cita): Result<Boolean>
    suspend fun deleteCita(cita: Cita): Result<Boolean>
}