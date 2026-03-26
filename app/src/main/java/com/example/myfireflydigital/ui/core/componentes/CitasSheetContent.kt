package com.example.myfireflydigital.ui.core.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myfireflydigital.domain.model.Cita

@Composable
fun CitasSheetContent(citas: List<Cita>, isLoading: Boolean) {
    Column (modifier = Modifier.fillMaxHeight(0.5f)){
        Text(
            text = "Citas Programadas",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 20.dp,bottom = 10.dp)
        )
        if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (citas.isEmpty()) {
                item { Text("No hay citas cercanas", modifier = Modifier.padding(16.dp)) }
            } else {
                items(citas) { cita ->
                    CitaCardItem(cita)
                }
            }
        }
    }

}

@Composable
fun CitaCardItem(cita: Cita) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = cita.titulo, style = MaterialTheme.typography.titleSmall)
                Text(text = cita.direccion, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}