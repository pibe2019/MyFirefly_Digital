package com.example.myfireflydigital.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.myfireflydigital.data.local.entities.CitaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {

    @Query("SELECT * FROM Cita ORDER BY id DESC")
    fun getCitasObserve(): Flow<List<CitaEntity>>

    @Query("SELECT * FROM Cita WHERE id = :id")
    suspend fun getCitaById(id: Int): CitaEntity

    @Upsert
    suspend fun upsertCita(cita: CitaEntity) : Long

    @Delete
    suspend fun deleteCita(cita: CitaEntity) : Int

}