package com.example.myfireflydigital.ui.core.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.myfireflydigital.ui.core.util.replaceLast
import com.example.myfireflydigital.ui.core.navigation.model.MainDrawerUiState
import com.example.myfireflydigital.ui.core.navigation.model.MenuItemUi
import kotlinx.coroutines.launch


@Composable
fun MainModalDrawerScreen(
    backStack: NavBackStack<NavKey>,
    mainScreenModalDrawerViewModel: MainModalDrawerScreenViewModel = hiltViewModel()
) {
    val menuContentBackStack = rememberNavBackStack(RoutesScreens.MapCitasScreen)
    val uiState by mainScreenModalDrawerViewModel.uiState.collectAsStateWithLifecycle()
    val currentContentRouter = menuContentBackStack.lastOrNull()

    LaunchedEffect(currentContentRouter) {
        currentContentRouter?.let { router ->
            mainScreenModalDrawerViewModel.updateScreenForRouter(router)
        }
    }
    MainScreen(
        menuBackStack = menuContentBackStack,
        uiState = uiState,
        onFinishApp = {backStack.removeLastOrNull()},
        onDrawerStateChanged = mainScreenModalDrawerViewModel::onDrawerStateChanged,
        onDrawerItemClick = { clickItem ->
            mainScreenModalDrawerViewModel.onDrawerStateChanged(DrawerValue.Closed)
            menuContentBackStack.replaceLast(clickItem.route)
        },
        onToggleDrawer = mainScreenModalDrawerViewModel::toggleDrawer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    menuBackStack: NavBackStack<NavKey>,
    uiState: MainDrawerUiState,
    onFinishApp: () -> Unit,
    onDrawerStateChanged: (DrawerValue) -> Unit,
    onDrawerItemClick: (MenuItemUi) -> Unit,
    onToggleDrawer: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = rememberTopAppBarState())//nuevo
    val drawerState = rememberDrawerState(uiState.drawerValue, onDrawerStateChanged)
    val gestosHabilitados = !drawerState.isClosed
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerState = drawerState, gesturesEnabled = gestosHabilitados, drawerContent = { DrawerContent(menuItems = uiState.menuItems, selectedRouter = menuBackStack.lastOrNull(), onItemClick = onDrawerItemClick) }) {
        Scaffold(
            modifier = Modifier
                //.nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize(),
            topBar = { MainContentTopAppBar(title = uiState.screenTitle, scrollBehavior = scrollBehavior, onOpenDrawer = onToggleDrawer) }
        ) { innerPadding ->
            NavigationMenuDrawer(modifier = Modifier.padding(innerPadding).padding(horizontal = 5.dp), backStack = menuBackStack)
        }
        //AREA INVISIBLE
        if (drawerState.isClosed){
            Spacer(modifier = Modifier.fillMaxHeight()
                .width(28.dp) //ancho del area sensible al borde
                .pointerInput(Unit){
                    detectHorizontalDragGestures { change, dragAmount ->
                        if (dragAmount > 10f ) { // Detecta movimiento a la derecha (positivo)
                            change.consume()
                            scope.launch { drawerState.open() }
                        }
                    }
                })
        }

    }
}

@Composable
fun DrawerContent(menuItems : List<MenuItemUi>, selectedRouter: NavKey?, onItemClick: (MenuItemUi) -> Unit) {
    ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.8f), drawerTonalElevation = 10.dp) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Menu", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        menuItems.forEach { menuItemUi ->
            NavigationDrawerItem(
                label = {Text(menuItemUi.title)},
                icon = {
                    menuItemUi.icon?.let { icon ->
                    Icon(imageVector = icon, contentDescription = menuItemUi.title)
                    }
                },
                selected = menuItemUi.route == selectedRouter,
                onClick = {onItemClick(menuItemUi)},
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContentTopAppBar(title: String, scrollBehavior: TopAppBarScrollBehavior, onOpenDrawer: () -> Unit){
    CenterAlignedTopAppBar(modifier = Modifier.padding(horizontal = 5.dp).statusBarsPadding().clip(
        RoundedCornerShape(14.dp)), scrollBehavior = scrollBehavior, title = { Text(text = title) }, navigationIcon = {
        Icon(imageVector = Icons.Default.Menu,
            contentDescription = "menu",
            modifier = Modifier.clickable(onClick = onOpenDrawer))
    },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ))
}

@Composable
fun rememberDrawerState(
    state: DrawerValue,
    onDrawerStateChanged: (DrawerValue) -> Unit
): DrawerState {
    val drawerState = remember { DrawerState(state) }
    LaunchedEffect(state) {
        if (drawerState.currentValue != state) {
            if (state == DrawerValue.Open) drawerState.open() else drawerState.close()
        }
    }
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue != state) onDrawerStateChanged(drawerState.currentValue)
    }
    return drawerState
}