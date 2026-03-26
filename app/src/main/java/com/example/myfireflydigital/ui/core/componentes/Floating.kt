package com.example.myfireflydigital.ui.core.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Floating(addCitas : () -> Unit, modifier: Modifier = Modifier) {
    // Es vital pasar el modifier para que el Scaffold pueda mover el FAB hacia arriba
    // cuando aparezca un Snackbar.
    FloatingActionButton(
        modifier = modifier,
        onClick = { addCitas() }
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "nueva cita"
        )
    }
}
