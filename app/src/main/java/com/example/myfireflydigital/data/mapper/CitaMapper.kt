package com.example.myfireflydigital.data.mapper

import com.example.myfireflydigital.data.local.entities.CitaEntity
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.result.EstadoCita

fun CitaEntity.toDomain(): Cita = Cita(id, titulo, direccion, fecha, hora, latitud, longitud, EstadoCita.valueOf(estado))

fun Cita.toEntity(): CitaEntity = CitaEntity(id, titulo, direccion, fecha, hora, latitud, longitud, estado.name)