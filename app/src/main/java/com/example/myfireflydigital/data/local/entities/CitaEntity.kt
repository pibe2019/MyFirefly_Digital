package com.example.myfireflydigital.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cita")
data class CitaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val direccion: String,
    val fecha: String,
    val hora: String,
    val latitud: Double,
    val longitud: Double,
    val estado: String//EstadoCita = EstadoCita.NO_VISITADO
)
