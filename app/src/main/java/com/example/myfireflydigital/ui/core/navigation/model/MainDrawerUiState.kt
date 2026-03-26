package com.example.myfireflydigital.ui.core.navigation.model

import android.icu.text.CaseMap
import androidx.compose.material3.DrawerValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myfireflydigital.ui.core.navigation.RoutesScreens

data class MainDrawerUiState(
    val drawerValue : DrawerValue = DrawerValue.Closed,
    val screenTitle : String = "",
    val menuItems : List<MenuItemUi> = emptyList()
)

data class  MenuItemUi(
    val route: RoutesScreens,
    val title: String,
    val icon: ImageVector?,
)