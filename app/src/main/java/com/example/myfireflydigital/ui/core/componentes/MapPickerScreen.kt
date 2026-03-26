package com.example.myfireflydigital.ui.core.componentes

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.PlaceLocation
import com.example.myfireflydigital.domain.model.PlacePrediction
import com.example.myfireflydigital.ui.core.util.cleanInput
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapPickerScreen(
    addressQuery: String,
    placePredictions: List<PlacePrediction>,
    isLoadingSearchingPlace: Boolean,
    selectedLocation: PlaceLocation?,
    isReverseGeocoding: Boolean,
    isLocationManualAdjusted: Boolean,
    onCloseMapPicker: () -> Unit,
    onConfirmMapLocation: (Double, Double) -> Unit,
    onAddressQueryChange: (String) -> Unit,
    onPredictionSelected: (String) -> Unit,
    onMapMarkerMoved: (Double, Double) -> Unit){

    var hasMovedAtLeastOnce by remember { mutableStateOf(false) }
   //estado del mapa - por defecto lima
   val defaultLatLong = remember(selectedLocation) {
       LatLng(
           selectedLocation?.latitud ?: -12.121903,
           selectedLocation?.longitud ?: -77.030605
       )
   }
   val camaraPositionState = rememberCameraPositionState {position = CameraPosition.fromLatLngZoom(defaultLatLong, 13f)}//lejos
   //REECENTRAMOS EL MAPA
   LaunchedEffect(selectedLocation) {
       selectedLocation ?: return@LaunchedEffect
       val target = LatLng(selectedLocation.latitud, selectedLocation.longitud)
       // ACTUALIZAMOS EL MARKER
       if (!isLocationManualAdjusted) camaraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 14f), durationMs = 800)
   }
    //Detecta cuando el usuario deja de mover el mapa (isMoving = false)  y notifica al ViewModel con las coordenadas del centro de la cámara.
    LaunchedEffect(camaraPositionState) {
        snapshotFlow { camaraPositionState.isMoving }
            .collect { inMoving ->
                if (inMoving){
                    hasMovedAtLeastOnce= true // se empeso a mover el map
                } else if (hasMovedAtLeastOnce){
                    val center = camaraPositionState.position.target
                    onMapMarkerMoved(center.latitude, center.longitude)
                }
            }
    }

    Box(modifier = Modifier) {
        //MAPA
        GoogleMap(modifier = Modifier
            .fillMaxSize(),
            cameraPositionState = camaraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true
            )
        ) {
            /*Marker(
                state = markerState,
                title =  "ubicacion de la cita" ,
                draggable = false
            )*/
        }
        //PUNTO FIJO
        Icon(
            imageVector        = Icons.Default.LocationOn,
            contentDescription = "Ubicación seleccionada",
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .offset(y = (-24).dp)  // sube la mitad del ícono para que la punta quede en el centro
        )
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 5.dp, end = 1.dp).align(Alignment.TopStart)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(28.dp),shape = RoundedCornerShape(50), shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
                    IconButton(onClick = onCloseMapPicker) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(15), shadowElevation = 4.dp,color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = addressQuery,
                        onValueChange = { onAddressQueryChange(it) },
                        placeholder = {Text("Buscar Dirección ..")},
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        trailingIcon = {
                            if (isLoadingSearchingPlace || isReverseGeocoding) {
                                CircularProgressIndicator()
                            } else Icon(Icons.Default.LocationOn, contentDescription = "Localización", tint = MaterialTheme.colorScheme.primary)
                        }
                    )
                }
            }
            // Dropdown de predicciones
            AnimatedVisibility(visible = placePredictions.isNotEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth().height((placePredictions.size.coerceAtMost(4) * 64).dp).padding(top = 5.dp),shape = RoundedCornerShape(5), shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                    ) {
                        placePredictions.take(5).forEach { prediction ->
                            ListItem(
                                headlineContent = { Text(prediction.primaryText) },
                                supportingContent = {
                                    Text(
                                        prediction.secondaryText,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.LocationOn, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.clickable { onPredictionSelected(prediction.placeId) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
        //DIRECCION  + COORDENADAS
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ){
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                // Dirección
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "LOCATION",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isReverseGeocoding        -> "Obteniendo dirección..."
                                addressQuery.isNotBlank() -> addressQuery
                                else                      -> "Mueve el pin para seleccionar"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // Badge "ajustado manualmente"
                        if (isLocationManualAdjusted) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Posición ajustada manualmente",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                // Coordenadas actuales
                val currentLat = camaraPositionState.position.target.latitude
                val currentLng = camaraPositionState.position.target.longitude
                Text(
                    text = "%.6f, %.6f".format(currentLat, currentLng),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Botón confirmar
                Button(
                    onClick = {
                        // El centro de la cámara ES la posición del pin fijo
                        val center = camaraPositionState.position.target
                        onConfirmMapLocation(
                            center.latitude,
                            center.longitude
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isReverseGeocoding
                ){
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar ubicación")
                }
            }
        }
    }






}