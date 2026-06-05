package com.example.myfireflydigital.ui.detallecita

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfireflydigital.ui.core.componentes.EstadoCitaBadge
import com.example.myfireflydigital.ui.core.util.toBadgeConfig
import com.example.myfireflydigital.ui.modeloui.UiEffect
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCitaScreen(
    idCita: Int,
    detalleCitaViewModel: DetalleCitaViewModel = hiltViewModel<DetalleCitaViewModel, DetalleCitaViewModel.Factory>(
        key = idCita.toString(),
        creationCallback = { factory -> factory.create(idCita) })
) {
    val cita by detalleCitaViewModel.cita.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val (estadoLabel, estadoColor) = cita?.estado?.toBadgeConfig() ?: Pair("", MaterialTheme.colorScheme.surface)
    var showMap by rememberSaveable { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(cita?.latitud ?: 0.0, cita?.longitud ?: 0.0), 12f) }

    LaunchedEffect(cita) {
        cita?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitud, it.longitud), 15f)
        }
    }
    LaunchedEffect(Unit) {
        detalleCitaViewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                    effect.message.messageApp?.asString(
                        context
                    ) ?: "error"
                )
                is UiEffect.RequestGpsEnable -> Unit
            }
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(cita?.titulo ?: "") }, modifier = Modifier.fillMaxWidth()) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EstadoCitaBadge(estadoLabel = estadoLabel, estadoColor = estadoColor, onClickEstado = {estado->detalleCitaViewModel.onEstadoChanged(estado)})
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(cita?.fecha ?: "") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Fecha",
                                    modifier = Modifier.size(18.dp)
                                )
                            })
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(cita?.hora ?: "") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "Hora",
                                    modifier = Modifier.size(18.dp)
                                )
                            })
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 0.5.dp
                    )
                    ListItem(
                        headlineContent = { Text(text = cita?.direccion ?: "") },
                        overlineContent = { Text("UBICACION") },
                        //supportingContent = { Text("${cita?.fecha ?: ""} * ${cita?.hora ?: ""} ", color = MaterialTheme.colorScheme.onSurfaceVariant)},
                        leadingContent = {
                            Icon(
                                Icons.Default.Place,
                                tint = estadoColor,
                                contentDescription = "Localización"
                            )
                        },
                        //trailingContent = { AssistChip(onClick = {}, label = { Text(text = estadoLabel )}, colors = AssistChipDefaults.assistChipColors(containerColor = estadoColor)) }
                    )
                }
            }
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {showMap = !showMap}, modifier = Modifier.weight(1f)) {
                    Text(text = if(showMap) "ocultar" else "ver")
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Map, contentDescription = "ver lugar")
                }
                //BADGE DE COORDENADAS
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lat: ${
                            cita?.latitud.toString().take(7)
                        }\nLon:  ${cita?.longitud.toString().take(7)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
            if (showMap){
                //MAPA
                GoogleMap(
                    modifier = Modifier.weight(1f),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = false,
                        mapType = MapType.NORMAL
                    ),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = false)
                ) {
                    //MARCADORES DE TODAS LAS CITAS Y SE RESALTA EL MARKER DE LA CITA SEECCIONADA
                    Marker(
                        state = rememberMarkerState(position = LatLng(cita?.latitud ?: 0.0, cita?.longitud ?: 0.0)),
                        title= cita?.titulo,
                        snippet = cita?.direccion
                    )
                }
            }
        }
    }
}