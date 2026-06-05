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
