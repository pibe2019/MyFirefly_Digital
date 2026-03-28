package com.example.myfireflydigital.ui.detallecita

import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfireflydigital.ui.core.util.toBadgeConfig

@Composable
fun DetalleCitaScreen(idCita: Int, detalleCitaViewModel: DetalleCitaViewModel = hiltViewModel<DetalleCitaViewModel, DetalleCitaViewModel.Factory>(creationCallback = {factory -> factory.create(idCita)})) {
    val cita by detalleCitaViewModel.cita.collectAsStateWithLifecycle()
    val (estadoLabel, estadoColor) = cita?.estado?.toBadgeConfig() ?: Pair("", MaterialTheme.colorScheme.surface)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp, top = 5.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = cita?.estado?.toBadgeConfig()?.second?.copy(alpha = 0.15f) ?: MaterialTheme.colorScheme.surface,
                shape = CircleShape,
                border = BorderStroke(1.dp, cita?.estado?.toBadgeConfig()?.second?.copy(alpha = 0.5f) ?: MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = estadoLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = estadoColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = cita?.titulo ?: "",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(text = cita?.direccion ?: "", modifier = Modifier.fillMaxWidth())
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
                }
                //COORDENADAS
                if (cita?.latitud != null && cita?.longitud != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Coordenadas: ${cita?.latitud}, ${cita?.longitud}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        //FECHA-HORA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = cita?.fecha ?: "",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = cita?.hora ?: "",
                modifier = Modifier.weight(1f),
            )
        }
    }




}