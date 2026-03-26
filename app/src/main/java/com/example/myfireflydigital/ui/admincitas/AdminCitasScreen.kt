package com.example.myfireflydigital.ui.admincitas

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.ui.core.componentes.AddCitasSheet
import com.example.myfireflydigital.ui.core.componentes.Floating
import com.example.myfireflydigital.ui.core.componentes.MapPickerScreen
import com.example.myfireflydigital.ui.modeloui.AdminCitasEvent
import com.example.myfireflydigital.ui.modeloui.CitasUiEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCitasScreen(
    adminCitasViewModel: AdminCitasViewModel = hiltViewModel()
) {
    val uiState by adminCitasViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHosState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        adminCitasViewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CitasUiEffect.ShowSnackbar -> {
                    launch {//corrutina independiente
                        snackbarHosState.showSnackbar(
                            message = effect.message.messageApp?.asString(context)
                                ?: "Operacion exitosa",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //PANTALLA LISTA DE CITAS
        Box(modifier = Modifier.fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(uiState.citas, key = { _, cita -> cita.id }) { index, cita ->
                        CitaItem(
                            cita = cita,
                            onDelete = { adminCitasViewModel.onEvent(AdminCitasEvent.OnDeleteCita(cita)) },
                            onLongClick = {adminCitasViewModel.onEvent(AdminCitasEvent.OnLongPressCitaOpenSheet(cita.id)) }
                        )
                    }
                }
            }
            // FAB posicionado manualmente
            Floating(
                addCitas = { adminCitasViewModel.onEvent(AdminCitasEvent.OnOpenSheet) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )

        }
        //SHEET CREAR-EDITAR
        if (uiState.isSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = {  adminCitasViewModel.onEvent(AdminCitasEvent.OnCloseSheet) },
                sheetState = sheetState,
                dragHandle = null
            ) {
                    AddCitasSheet(
                        onFormNewCita = {citaNueva -> adminCitasViewModel.onEvent(AdminCitasEvent.OnFormNew(citaNueva))},
                        onCitaSelect = uiState.citaSelectEnEdicion,// null:crear, cita: editar
                        addressQuery = uiState.addressQuery,//cargado de info pero si es nuevo no
                        selectedLocation = uiState.selectedLocation,
                        openMapPicker = { adminCitasViewModel.onEvent(AdminCitasEvent.OnOpenMapPicker) },
                        onGuardar = { cita ->
                            adminCitasViewModel.onEvent(
                                AdminCitasEvent.OnUpsertCita(cita)
                            )
                        },
                        onCancelar = { adminCitasViewModel.onEvent(AdminCitasEvent.OnCloseSheet) }
                    )
            }
        }
        //MAP PICKER  controlado por isMapPickerVisible
        AnimatedVisibility(
            visible = uiState.isMapPikerVisible,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()) {
            BackHandler(enabled = uiState.isMapPikerVisible) {
                adminCitasViewModel.onEvent(AdminCitasEvent.OnCloseMapPicker)
            }
            MapPickerScreen(
                //citaSelect = uiState.citaSeleccionada,//este puede tener las coordenadas y direccion si solo va a editar
                addressQuery = uiState.addressQuery, //CON INFO CUANDO EDITAS
                placePredictions = uiState.placePredictions,
                isLoadingSearchingPlace = uiState.isLoadingSearchingPlace,
                selectedLocation = uiState.selectedLocation,//CON INFO CUANDO EDITAS despues null
                isReverseGeocoding = uiState.isReverseGeocoding,
                isLocationManualAdjusted = uiState.isLocationManualAdjusted,
                onCloseMapPicker = { adminCitasViewModel.onEvent(AdminCitasEvent.OnCloseMapPicker) },
                onConfirmMapLocation = { lat, lng -> adminCitasViewModel.onEvent(AdminCitasEvent.OnConfirmMapLocation(lat, lng)) },
                onAddressQueryChange = { adminCitasViewModel.onEvent(AdminCitasEvent.OnAddressQueryChanged(it)) },
                onPredictionSelected = { adminCitasViewModel.onEvent(AdminCitasEvent.OnPredictionSelected(it)) },
                onMapMarkerMoved = { lat, long -> adminCitasViewModel.onEvent(AdminCitasEvent.OnMapMarkerMoved(lat, long)) }
            )
        }
        SnackbarHost(snackbarHosState) { data ->
            Snackbar(
                modifier = Modifier.padding(12.dp), snackbarData = data
            )
        }
    }
}

@Composable
fun CitaItem(
    cita: Cita,
    onDelete: () -> Unit,
    onLongClick: () -> Unit/*, onLongClick: () -> Unit*/
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongClick),
        //onClick = onLongClick ,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cita.titulo, style = MaterialTheme.typography.titleMedium)
                Text(text = cita.fecha, style = MaterialTheme.typography.bodySmall)
                Text(text = cita.estado.name, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}