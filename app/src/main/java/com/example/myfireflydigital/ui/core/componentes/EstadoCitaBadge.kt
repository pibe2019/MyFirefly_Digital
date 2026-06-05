package com.example.myfireflydigital.ui.core.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myfireflydigital.domain.model.result.EstadoCita
import com.example.myfireflydigital.ui.core.util.toBadgeConfig

@Composable
fun EstadoCitaBadge(estadoLabel: String, estadoColor: Color, onClickEstado: (EstadoCita) -> Unit) {
    var expandedEstado by remember { mutableStateOf(false) }
    Box {
        Surface(
            color = estadoColor.copy(alpha = 0.12f),
            contentColor = estadoColor,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.wrapContentSize(),
            tonalElevation = 0.dp,
            onClick = {expandedEstado = true}
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "icono cita",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = estadoLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        DropdownMenu(expanded = expandedEstado, onDismissRequest = {expandedEstado = false}) {
            EstadoCita.entries.forEach { estado ->
                val (label, color) = estado.toBadgeConfig()
                DropdownMenuItem(text = {Text(text=label,color= color)}, onClick ={
                    onClickEstado(estado)
                    expandedEstado = false
                } )
            }
        }
    }
}