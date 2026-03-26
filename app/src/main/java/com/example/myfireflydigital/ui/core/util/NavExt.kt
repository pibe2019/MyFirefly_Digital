package com.example.myfireflydigital.ui.core.util

import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.myfireflydigital.domain.model.result.EstadoCita

private var lastNavTime = 0L // almacena el momento exacto del ultimo clic exitoso
private const val DEBOUNCE_TIME_MS = 400L //MILISEGUNDOS DE proteccion 0.4 segundos

private fun canNavigate(): Boolean {
    val currentTime = System.currentTimeMillis()// cantidad de milisegundos, momento actual
    val cannNavigate = currentTime - lastNavTime > DEBOUNCE_TIME_MS
    if (cannNavigate) {
        lastNavTime = currentTime
        return true
    }
    return false
}
fun NavBackStack<NavKey>.replaceLast(screen: NavKey) =
    takeIf { canNavigate() }?.apply {
        if (isNotEmpty()) removeLastOrNull()
        add(screen)
    }

fun EstadoCita.toBadgeConfig(): Pair<String, Color> = when(this){
    EstadoCita.VISITADO ->  "VISITADO" to Color(0xFF4CAF50)
    EstadoCita.EN_RUTA -> "EN RUTA" to Color(0xFF4FC3F7)
    EstadoCita.NO_VISITADO -> "NO VISITADO" to Color(0xFFF44336)
    EstadoCita.CANCELADO -> "CANCELADO" to Color.Gray
}
