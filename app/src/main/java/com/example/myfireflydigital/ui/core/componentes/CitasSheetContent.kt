package com.example.myfireflydigital.ui.core.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myfireflydigital.domain.model.Cita
import com.example.myfireflydigital.ui.core.util.toBadgeConfig

@Composable
fun CitasSheetContent(
    citas: List<Cita>,
    isLoadingCitas: Boolean,
    onClickCita: (Cita) -> Unit,
    citaSelecId: Int?,
    onDetalleClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight(0.5f)) {
        Text(
            text = "Citas Programadas",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)
        )
        if (isLoadingCitas) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (citas.isEmpty()) {
                item { Text("No hay citas cercanas", modifier = Modifier.padding(16.dp)) }
            } else {
                items(citas, key = { it.id }) { cita ->

                    CitaCardItem(
                        cita = cita,
                        onClick = { onClickCita(cita) },
                        isSelected = cita.id == citaSelecId,
                        onDetalleClick = {onDetalleClick(cita.id)}
                    )
                }
            }
        }
    }

}

@Composable
fun CitaCardItem(cita: Cita, onClick: () -> Unit, isSelected: Boolean, onDetalleClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {}),
        onClick = { onClick() },
        enabled = cita.latitud != 0.0,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = cita.titulo, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = cita.estado.toBadgeConfig().second),
                        modifier = Modifier,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = cita.estado.toBadgeConfig().first,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        modifier = Modifier.size(14.dp),
                        contentDescription = null,
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = cita.direccion, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(text = cita.hora, style = MaterialTheme.typography.bodySmall)
                }
                Text(text = cita.fecha, style = MaterialTheme.typography.bodySmall)
                Row() {
                    TextButton(
                        onClick = {},
                        modifier = Modifier,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text(
                            text = "Cancelar Visita",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDetalleClick, modifier = Modifier) {
                        Text(
                            text = "Detalle",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

            }
        }
    }
}