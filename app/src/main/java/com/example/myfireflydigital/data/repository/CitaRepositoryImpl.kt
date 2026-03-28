package com.example.myfireflydigital.data.repository

import com.example.myfireflydigital.data.local.dao.CitaDao
import com.example.myfireflydigital.data.mapper.toDomain
import com.example.myfireflydigital.data.mapper.toEntity
import com.example.myfireflydigital.di.IoDispatcher
import com.example.myfireflydigital.domain.exceptions.LocalStorageException
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.repository.CitaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CitaRepositoryImpl @Inject constructor(private val citaDaoApi: CitaDao, @IoDispatcher private val ioDispatcher: CoroutineDispatcher) : CitaRepository {

    override fun getCitasObserve(): Flow<List<Cita>> {
        return citaDaoApi.getCitasObserve().map { citaEntities ->
            citaEntities.map { it.toDomain() }
        }.catch {
            emit(emptyList())
        }.flowOn(ioDispatcher)
    }

    override suspend fun getCitaById(id: Int): Result<Cita?> = withContext(ioDispatcher){
            runCatching {
                citaDaoApi.getCitaById(id).toDomain()
            }.recoverCatching {
                if (it is CancellationException) throw it
                throw LocalStorageException("${it.message}")
            }
        }

    override suspend fun upsertCita(cita: Cita): Result<Boolean> = withContext(ioDispatcher){
            runCatching {
                val result = citaDaoApi.upsertCita(cita.toEntity())
                result > 0
            }.recoverCatching {
                if (it is CancellationException) throw it
                throw LocalStorageException("${it.message}")
            }
        }

    override suspend fun deleteCita(cita: Cita): Result<Boolean> = withContext(ioDispatcher){
            runCatching {
                val result = citaDaoApi.deleteCita(cita.toEntity())
                result == 1
            }.recoverCatching {
                if (it is CancellationException) throw it
                throw LocalStorageException("${it.message}")
            }
        }
}