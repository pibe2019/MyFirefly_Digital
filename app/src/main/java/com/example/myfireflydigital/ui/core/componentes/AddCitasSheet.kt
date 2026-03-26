package com.example.myfireflydigital.ui.core.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.domain.model.PlaceLocation
import com.example.myfireflydigital.ui.core.util.cleanInput

@Composable
fun AddCitasSheet(
    onFormNewCita: (Cita) -> Unit,
    onCitaSelect: Cita? = null,
    addressQuery: String,
    selectedLocation: PlaceLocation?,
    openMapPicker: () -> Unit,
    onGuardar: (Cita) -> Unit,
    onCancelar: () -> Unit
) {
    val cita = onCitaSelect ?: return
    val esEdicion = onCitaSelect.titulo.isNotEmpty()

    //var titulo by rememberSaveable() { mutableStateOf(onCitaSelect?.titulo ?: "") }

    val latitud = selectedLocation?.latitud?.toString() ?: cita.latitud.takeIf { it != 0.0 }?.toString() ?: ""
    val longitud = selectedLocation?.longitud?.toString() ?: cita.longitud.takeIf { it != 0.0 }?.toString() ?: ""
    //DIRECCION A MOSTRAR
    val direccionMostrada = when {
        addressQuery.isNotEmpty() -> addressQuery
        selectedLocation?.address?.isNotEmpty() == true -> selectedLocation.address//lugar final x buscador o mapa
        cita.direccion.isNotEmpty() -> cita.direccion//selecciono una cita para edit o no:null? direccio original
        else -> ""
    }

    // errores de validación
    var tituloError by rememberSaveable { mutableStateOf(false) }
    val direccionError by rememberSaveable { mutableStateOf(false) } //-- EN PRUEBA

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp, top = 5.dp)
    ) {
        Text(
            text = if (esEdicion) "Editar cita" else "Nueva cita",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
        /*TITULO*/
        OutlinedTextField(
            value = cita.titulo,
            onValueChange = { onFormNewCita(cita.copy(titulo= it)); tituloError = false },
            label = { Text("Título") },
            isError = tituloError,
            supportingText = { if (tituloError) Text("Campo requerido") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text(text = "Ubicacion")
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Localización",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = direccionMostrada.ifEmpty { "Sin ubicacion seleccionada" },
                        color = if (direccionMostrada.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
                //COORDENADAS
                if (latitud.isNotEmpty() && longitud.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Coordenadas: $latitud, $longitud",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = openMapPicker) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Localización", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if(direccionMostrada.isEmpty()) "Seleccionar en mapa" else "Actualizar ubicación")
                }
            }
        }

        //FECHA-HORA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = cita.fecha,
                onValueChange = { onFormNewCita(cita.copy(fecha=it)) },
                label = { Text("Fecha") },
                placeholder = { Text("DD/MM/AAAA") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = cita.hora,
                onValueChange = { onFormNewCita(cita.copy(hora = it)) },
                label = { Text("Hora") },
                placeholder = { Text("HH:MM") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        //BOTONES
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(onClick = {
                tituloError = cita.titulo.isBlank()
                //direccionError = direccion.isBlank()
                if (!tituloError) {
                    onGuardar(
                        cita.copy(
                            direccion = selectedLocation?.address ?: addressQuery.cleanInput(),
                            latitud = latitud.toDoubleOrNull() ?: 0.0,
                            longitud = longitud.toDoubleOrNull() ?: 0.0                        )
                    )
                }
            }, modifier = Modifier.weight(1f)) {
                Text(if (esEdicion) "Actualizar" else "Guardar")
            }
        }
    }
}