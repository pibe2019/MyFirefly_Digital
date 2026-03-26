package com.example.myfireflydigital.ui.core.navigation


import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import com.example.myfireflydigital.ui.core.navigation.model.MainDrawerUiState
import com.example.myfireflydigital.ui.core.navigation.model.MenuItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainModalDrawerScreenViewModel @Inject constructor( ) : ViewModel() {
    private val _uiState = MutableStateFlow(
        MainDrawerUiState(drawerValue = DrawerValue.Closed,
            screenTitle = RoutesScreens.MapCitasScreen.title,
            menuItems = RoutesScreens.menuItems.map { route ->
                MenuItemUi(
                    route = route,
                    title = route.title,
                    icon = route.icon
                )
            })
    )
    val uiState : StateFlow<MainDrawerUiState> = _uiState.asStateFlow()

    //init{loadStaticMenu()}
    fun onDrawerStateChanged(newState : DrawerValue){
        _uiState.update { it.copy(drawerValue = newState) }
    }

    fun toggleDrawer(){
        val newState = if(_uiState.value.drawerValue == DrawerValue.Open) DrawerValue.Closed else DrawerValue.Open
        _uiState.update { it.copy(drawerValue = newState) }
    }

    // actualizar el titulo
    fun updateScreenForRouter(route: NavKey?){
        val currentScreen = route as? RoutesScreens ?: return
        _uiState.update { currentState ->
            currentState.copy(screenTitle = currentScreen.title)
        }
    }

    /*private fun loadStaticMenu(){
        val menuItems = RoutesScreens.menuItems.map { menuItemRoute ->
            MenuItemUi(
                route = menuItemRoute,
                title = menuItemRoute.title,
                icon = menuItemRoute.icon
            )
        }
        _uiState.update { it.copy(menuItems = menuItems, screenTitle = RoutesScreens.MapCitasScreen.title) }
    }*/

}