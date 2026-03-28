package com.example.myfireflydigital.ui.mapcitas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfireflydigital.ui.core.componentes.CitasSheetContent
import com.example.myfireflydigital.ui.modeloui.MapCitasEvent
import com.example.myfireflydigital.ui.modeloui.UiEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapCitasScreen(
    mapCitasViewModel: MapCitasViewModel = hiltViewModel()
) {
    val uiMapState by mapCitasViewModel.uiMapState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()
    // 1:estado del permiso
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    // 2. Estado de la Cámara
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(-8.07, -79.11), 12f) }
    // 3. CUANDO SE CONCEDE EL PERMISO AVISAR AL ViewModel
    LaunchedEffect(locationPermissionState.status) {
        if (!locationPermissionState.status.isGranted) showDialog = true
        else mapCitasViewModel.OnEvent(MapCitasEvent.OnMyLocation)//.onCurrentLocation()
    }
    LaunchedEffect(uiMapState.userLocation) {
        uiMapState.userLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }
    LaunchedEffect(Unit) {
        mapCitasViewModel.effect.collectLatest {  effect ->
            when(effect){
                is UiEffect.ShowSnackbar -> {
                    val mensajeSnack = effect.message.messageApp?.asString(context) ?: return@collectLatest
                    snackbarHostState.showSnackbar(mensajeSnack)
                }
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 250.dp,
        sheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        snackbarHost = {SnackbarHost(snackbarHostState)},
        sheetContent = {
            CitasSheetContent(uiMapState.citas, uiMapState.isLoadingCitas, onClickCita = {cita -> mapCitasViewModel.OnEvent(MapCitasEvent.OnSelectCita(cita))
            }, citaSelecId = uiMapState.citaSelecId)
        }
    ){ paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (locationPermissionState.status.isGranted){
                MapsCitas(
                    cameraPositionState = cameraPositionState,
                    properties = uiMapState.properties,
                    userLocation = uiMapState.userLocation,
                    onMapLoaded = {mapCitasViewModel.OnEvent(MapCitasEvent.OnMapLoaded)}
                )
            }else {
                Box(Modifier.fillMaxSize().background(Color.DarkGray))
            }
            if (showDialog && (locationPermissionState.status.shouldShowRationale || !locationPermissionState.status.isGranted)){
                //denego o no lo tengo
                LocationPermissionRequestDialog(locationPermissionState, onDismiss = {showDialog=false})
            }
            if (uiMapState.isLoadingMap) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            if (uiMapState.isLoadingRouteUbi) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).background(Color.Green))
        }
    }

}

@Composable
fun MapsCitas(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    properties: MapProperties,
    userLocation: LatLng?,
    onMapLoaded: ()->Unit
) {
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = MapUiSettings(myLocationButtonEnabled = true),
        onMapLoaded = onMapLoaded
    ) {
        userLocation?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Mi ubicación",
                snippet = "peru"
            )
        }

    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocationPermissionRequestDialog(
    permissionState: PermissionState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Permiso de ubicación requerido",style = MaterialTheme.typography.titleLarge)
        },
        text = {Text("Necesitamos tu ubicación para dibujar la ruta mas cercana.")},
        confirmButton = {
            Button(onClick = {
                permissionState.launchPermissionRequest()
                onDismiss()
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss){Text(text="Ahora no")} }

        )
}