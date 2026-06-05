package com.example.myfireflydigital.ui.core.util

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.ui.graphics.Color
import com.example.myfireflydigital.domain.model.result.EstadoCita
import java.time.Instant
import java.time.LocalDate.parse
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

fun String.cleanInput() : String = this.trim()

fun EstadoCita.toBadgeConfig(): Pair<String, Color> = when(this){
    EstadoCita.VISITADO ->  "VISITADO" to Color(0xFF4CAF50)
    EstadoCita.EN_RUTA -> "EN RUTA" to Color(0xFF4FC3F7)
    EstadoCita.NO_VISITADO -> "NO VISITADO" to Color(0xffd51e34)
    EstadoCita.CANCELADO -> "CANCELADO" to Color.Gray
}

//DE STRING A LONG
fun String.toDateMillis(pattern: String = "dd/MM/yyyy"): Long? {
    return try {
        parse(this, DateTimeFormatter.ofPattern(pattern))
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    } catch (e: Exception) { null }
}
//DE LONG A STRING
fun Long?.toDateStringOrNull(formato: String = "dd/MM/yyyy"): String? {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern(formato))
    }
}

//String a TimeState
fun String.toHourAndMinute(): TimeState? {
    return try {
        val parts = this.split(":")
        if (parts.size == 2) {
            TimeState(
                hour = parts[0].toInt().coerceIn(0, 23),
                minute = parts[1].toInt().coerceIn(0, 59)
            )
        } else null
    } catch (e: Exception) {
        null
    }
}

//Obtiene la HORA ACTUAL
fun getCurrentTime(): TimeState{
    val currentTime = Calendar.getInstance()
    return TimeState(currentTime[Calendar.HOUR_OF_DAY], currentTime[Calendar.MINUTE])
}

//TimePickerState a String formateado
@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerState.toTimeString(): String {
    return String.format(Locale.getDefault(), HOUR_MINUTE_FORMAT, this.hour, this.minute)
}
private const val HOUR_MINUTE_FORMAT = "%02d:%02d"

//DATA CLASS AUXILIAR
data class  TimeState(val hour: Int, val minute: Int)
