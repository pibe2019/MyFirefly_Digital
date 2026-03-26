package com.example.myfireflydigital.data.repository

import com.example.myfireflydigital.data.local.dao.CitaDao
import com.example.myfireflydigital.data.mapper.toDomain
import com.example.myfireflydigital.data.mapper.toEntity
import com.example.myfireflydigital.domain.exceptions.LocalStorageException
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.repository.CitaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CitaRepositoryImpl (private val citaDaoApi: CitaDao) : CitaRepository {

    override fun getCitasObserve(): Flow<List<Cita>> {
        return citaDaoApi.getCitasObserve().map { citaEntities ->
            citaEntities.map { it.toDomain() }
        }.catch { emit(emptyList()) }
    }

    override suspend fun getCitaById(id: Int): Result<Cita?> {
        return runCatching {
            citaDaoApi.getCitaById(id).toDomain()
        }.recoverCatching { throw LocalStorageException("$it") }
    }

    override suspend fun upsertCita(cita: Cita): Result<Boolean> {
        return runCatching {
            val result = citaDaoApi.upsertCita(cita.toEntity())
            result > 0
        }.recoverCatching { throw LocalStorageException("$it") }
    }

    override suspend fun deleteCita(cita: Cita): Result<Boolean> {
        return runCatching {
            val result = citaDaoApi.deleteCita(cita.toEntity())
            result == 1
        }.recoverCatching { throw LocalStorageException("$it") }
    }
}