package com.example.myfireflydigital.ui.mapcitas

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.RouteResult
import com.example.myfireflydigital.ui.core.componentes.CitasSheetContent
import com.example.myfireflydigital.ui.modeloui.MapCitasEvent
import com.example.myfireflydigital.ui.modeloui.RouteInfo
import com.example.myfireflydigital.ui.modeloui.UiEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapCitasScreen(
    mapCitasViewModel: MapCitasViewModel = hiltViewModel(),
    onNavigateToDetalleCita: (Int) -> Unit
) {
    val uiMapState by mapCitasViewModel.uiMapState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()
    // 1:estado del permiso
    val locationPermissionState = rememberMultiplePermissionsState(listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    val locationGranted = locationPermissionState.permissions.any { it.status.isGranted } //alguno de los 2 esta selecionada (precisa, cercana)
    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            mapCitasViewModel.onEvent(MapCitasEvent.OnMyLocation)  // GPS activado → directo
        }
        // RESULT_CANCELED → silencio, el botón sigue disponible
    }


    // 3. CUANDO SE CONCEDE EL PERMISO AVISAR AL ViewModel
    LaunchedEffect(locationGranted) {
        if (locationGranted) mapCitasViewModel.onEvent(MapCitasEvent.OnMyLocation)//.onCurrentLocation()
        showDialog = !locationGranted
    }

    LaunchedEffect(Unit) {
        mapCitasViewModel.effect.collectLatest {  effect ->
            when(effect){
                is UiEffect.ShowSnackbar -> {
                    val mensajeSnack = effect.message.messageApp?.asString(context) ?: return@collectLatest
                    snackbarHostState.showSnackbar(mensajeSnack)
                }
                is UiEffect.RequestGpsEnable -> gpsLauncher.launch(IntentSenderRequest.Builder(effect.intentSender).build()) //dialog in-app
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 250.dp,
        sheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        snackbarHost = {SnackbarHost(snackbarHostState)},
        sheetContent = {
            CitasSheetContent(uiMapState.citas, uiMapState.isCitasLoaded, onClickCita = {cita -> mapCitasViewModel.onEvent(MapCitasEvent.OnSelectCita(cita))
            }, citaSelecId = uiMapState.citaSelecId,onDetalleClick = onNavigateToDetalleCita, onCancelarCita = {citaId -> mapCitasViewModel.onEvent(MapCitasEvent.OnCancelarCita(citaId)) })
        }
    ){ paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MapsCitas(
                userLocation = uiMapState.userLocation,
                permissionGranted = locationGranted,
                locationUpdateTick = uiMapState.locationUpdateTick,
                isMapLoaded = uiMapState.isMapLoaded,
                onMapLoaded = {mapCitasViewModel.onEvent(MapCitasEvent.OnMapLoaded)},
                citas = uiMapState.citas,
                routeInfo = uiMapState.routeInfo,
                citaSelecId = uiMapState.citaSelecId,
                onMyLocationButtonClick = {mapCitasViewModel.onEvent(MapCitasEvent.OnMyLocationButtonClick)}
            )
            if (showDialog && (locationPermissionState.shouldShowRationale || !locationGranted)){
                //denego o no lo tengo
                LocationPermissionRequestDialog(locationPermissionState, onDismiss = {showDialog=false})
            }
            //if (uiMapState.isLoadingMap) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            if (uiMapState.isLoadingRouteUbi) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).background(Color.Green))
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !uiMapState.isMapLoaded || !uiMapState.isCitasLoaded,
            modifier = Modifier.matchParentSize(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

}

@Composable
fun MapsCitas(
    modifier: Modifier = Modifier,
    permissionGranted: Boolean,
    userLocation: LatLng?,
    isMapLoaded: Boolean,
    onMapLoaded: () -> Unit,
    locationUpdateTick: Int,
    citas: List<Cita> = emptyList(),
    routeInfo: RouteResult? = null,
    citaSelecId: Int? = null,
    onMyLocationButtonClick: () -> Unit
) {
    //var isMapLoaded by rememberSaveable { mutableStateOf(false) }//esta cargado el mapa?
    val properties = remember(permissionGranted)  { MapProperties(isMyLocationEnabled = permissionGranted, mapType = MapType.NORMAL) }
    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = false,mapToolbarEnabled = false, myLocationButtonEnabled = true) }
    // Estado de la Cámara
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(-8.07, -79.11), 12f) }
    LaunchedEffect(locationUpdateTick,isMapLoaded) {
        if (isMapLoaded) {
            userLocation?.let { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f)) }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings,
            onMapLoaded = onMapLoaded,//onMapLoaded,
            onMyLocationButtonClick = {
                onMyLocationButtonClick()
                true
            }
        ) {
            //MARCADORES DE TODAS LAS CITAS Y SE RESALTA EL MARKER DE LA CITA SEECCIONADA
            citas.forEach { cita ->
                Marker(
                    state = MarkerState(position = LatLng(cita.latitud, cita.longitud)),
                    title = cita.titulo,
                    snippet = cita.direccion,
                    alpha = if (citaSelecId == null || citaSelecId == cita.id) 1f else 0.5f
                )
            }
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Mi ubicación",
                    snippet = "peru"
                )
            }

            if (routeInfo != null) {
                Polyline(
                    points = routeInfo.routePoints,
                    color = Color(0xFF1A73E8),//Azul de Google
                    width = 10f,
                    geodesic = true
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocationPermissionRequestDialog(
    permissionState: MultiplePermissionsState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
        title = {
            Text(text = "Permiso de ubicación requerido",style = MaterialTheme.typography.titleLarge)
        },
        text = {Text("Necesitamos tu ubicación para dibujar la ruta mas cercana.")},
        confirmButton = {
            Button(onClick = {
                permissionState.launchMultiplePermissionRequest()
                onDismiss()
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss){Text(text="Ahora no")} }

        )
}