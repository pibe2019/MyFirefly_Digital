package com.example.myfireflydigital.data.mapper

import com.example.myfireflydigital.data.local.entities.CitaEntity
import com.example.myfireflydigital.data.remote.dto.RouteDto
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.RouteResult
import com.example.myfireflydigital.domain.model.result.EstadoCita
import com.google.maps.android.PolyUtil

fun CitaEntity.toDomain(): Cita = Cita(id, titulo, direccion, fecha, hora, latitud, longitud, EstadoCita.valueOf(estado))

fun Cita.toEntity(): CitaEntity = CitaEntity(id, titulo, direccion, fecha, hora, latitud, longitud, estado.name)

fun RouteDto.toDomain(): RouteResult = RouteResult(
    points = PolyUtil.decode(polyline?.encodedPolyline.orEmpty()),
    distance = formatDistance(distanceMeters),
    duration = formatDuration(duration)
)
private fun formatDistance(distMeters: Int): String{
    return if (distMeters >= 1000) "${"%.1f".format(distMeters / 1000.0)} km"
    else "$distMeters m"
}

private fun formatDuration(durationSec: String): String{
    val seconds = durationSec.removeSuffix("s").toIntOrNull() ?: 0
    val minutes = seconds / 60
    return if(minutes >= 60) "${minutes/60} h ${minutes%60} min"
    else "$minutes min"
}