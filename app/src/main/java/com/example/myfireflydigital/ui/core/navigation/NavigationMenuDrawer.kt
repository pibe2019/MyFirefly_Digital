package com.example.myfireflydigital.ui.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.myfireflydigital.ui.admincitas.AdminCitasScreen
import com.example.myfireflydigital.ui.detallecita.DetalleCitaScreen
import com.example.myfireflydigital.ui.mapcitas.MapCitasScreen

@Composable
fun NavigationMenuDrawer(modifier: Modifier, backStack: NavBackStack<NavKey>,) {
    NavDisplay(
        modifier = modifier,//IMPORTANTE - ENTREGA EL innerPadding a todo el contexto de la estructura de las vistas
        backStack = backStack,
        onBack = {backStack.removeLastOrNull()},
        entryProvider = entryProvider {
            entry<RoutesScreens.MapCitasScreen> { MapCitasScreen() }
            entry <RoutesScreens.DetalleCitasSreen>{ DetalleCitaScreen() }
            entry <RoutesScreens.AdminCitasScreen>{ AdminCitasScreen() }
        }
    )
}