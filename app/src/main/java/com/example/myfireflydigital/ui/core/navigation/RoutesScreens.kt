package com.example.myfireflydigital.ui.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
sealed class RoutesScreens(var title: String, @Contextual var icon: ImageVector?= null) : NavKey{

    @Serializable
    data object MainModalDrawerScreen: RoutesScreens("Citas", null)
    @Serializable
    data object  MapCitasScreen: RoutesScreens("Citas Map", null)
    @Serializable
    data object  AdminCitasScreen: RoutesScreens("Admin Citas", null)
     @Serializable
     data class DetalleCitasSreen(val id: Int): RoutesScreens("Cita Detalle", null)

    companion object{
        val menuItems = listOf(
            MapCitasScreen,
            AdminCitasScreen,
            //DetalleCitasSreen
        )
    }
}