package com.example.myfireflydigital.domain.model

import com.example.myfireflydigital.domain.model.result.EstadoCita

data class Cita(val id: Int = 0,
                val titulo: String,
                val direccion: String,
                val fecha: String,
                val hora: String,
                val latitud: Double,
                val longitud: Double,
                val estado: EstadoCita = EstadoCita.NO_VISITADO)
