package com.example.myfireflydigital.ui.admincitas

import android.content.Context
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.ui.core.componentes.AddCitasSheet
import com.example.myfireflydigital.ui.core.componentes.Floating
import com.example.myfireflydigital.ui.core.componentes.MapPickerScreen
import com.example.myfireflydigital.ui.core.componentes.Search
import com.example.myfireflydigital.ui.core.util.toBadgeConfig
import com.example.myfireflydigital.ui.modeloui.AdminCitasEvent
import com.example.myfireflydigital.ui.modeloui.AdminCitasUiState
import com.example.myfireflydigital.ui.modeloui.UiEffect
import kotlinx.coroutines.flow.Flow
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AdminCitasEffectHandler(uiEffect = adminCitasViewModel.uiEffect, snackbarHosState = snackbarHosState, context = context)

    Box(modifier = Modifier.fillMaxSize()) {
        //PANTALLA LISTA DE CITAS
        CitasLisContent(uiState = uiState,onEvent = adminCitasViewModel::onEvent)
        //SHEET CREAR-EDITAR
        if (uiState.isSheetVisible) {
            CitasBottomSheet(uiState = uiState, sheetState = sheetState, onEvent = (adminCitasViewModel::onEvent), onDismiss = { keyboardController?.hide(); adminCitasViewModel.onEvent(AdminCitasEvent.OnCloseSheet) })
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
/** Maneja efectos One-Time, para q el AdminCitas.. no los mezcle con la UI **/
@Composable
private fun AdminCitasEffectHandler(uiEffect: Flow<UiEffect>, snackbarHosState: SnackbarHostState, context: Context) {
    LaunchedEffect(Unit) {
        uiEffect.collectLatest { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> {
                    launch {//corrutina independiente
                        snackbarHosState.showSnackbar(
                            message = effect.message.messageApp?.asString(context)
                                ?: "Operacion exitosa",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                is UiEffect.RequestGpsEnable -> Unit
            }
        }
    }
}

/** List + Search + Fab + Snackbar **/
@Composable
private fun CitasLisContent(uiState: AdminCitasUiState, onEvent: (AdminCitasEvent) -> Unit ){
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF8F9FA))
    ) {
        Column() {
            Search(searchQuery = uiState.searchQuery, onSearchQueryChanged = { query -> onEvent(AdminCitasEvent.OnSearchQueryChanged(query)) }, content = {})
            if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(uiState.citas, key = { _, cita -> cita.id }) { index, cita ->
                        CitaItem(
                            cita = cita,
                            onDelete = { onEvent(AdminCitasEvent.OnDeleteCita(cita)) },
                            onLongClick = { onEvent(AdminCitasEvent.OnLongPressCitaOpenSheet(cita.id)) }
                        )
                    }
                }
            }
        }

        // FAB posicionado manualmente
        Floating(
            addCitas = { onEvent(AdminCitasEvent.OnOpenSheet) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun CitaItem(
    cita: Cita,
    onDelete: () -> Unit,
    onLongClick: () -> Unit
) {
    val (label, color) = cita.estado.toBadgeConfig()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        //colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(color))
                Row(modifier = Modifier
                    .weight(1f)
                    .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = cita.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(text = cita.fecha, style = MaterialTheme.typography.bodySmall)
                        Text(text = label, style = MaterialTheme.typography.labelLarge, color = color)
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
}

/**************************************************************************************************/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitasBottomSheet(
    uiState: AdminCitasUiState,
    sheetState: SheetState,
    onEvent: (AdminCitasEvent) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        AddCitasSheet(
            onFormNewCita = {citaNueva -> onEvent(AdminCitasEvent.OnFormNew(citaNueva))},
            onCitaSelect = uiState.citaSelectEnEdicion,// null:crear, cita: editar
            addressQuery = uiState.addressQuery,//cargado de info pero si es nuevo no
            selectedLocation = uiState.selectedLocation,
            openMapPicker = { onEvent(AdminCitasEvent.OnOpenMapPicker) },
            onGuardar = { cita ->
                onEvent( AdminCitasEvent.OnUpsertCita(cita) )
            },
            onCancelar = onDismiss
        )
    }
}