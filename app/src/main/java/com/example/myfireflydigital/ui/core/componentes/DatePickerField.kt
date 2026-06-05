package com.example.myfireflydigital.ui.core.componentes

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.myfireflydigital.ui.core.util.toDateMillis
import com.example.myfireflydigital.ui.core.util.toDateStringOrNull
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun DatePickerField(fecha: String, onDateSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val initialMillis = remember(fecha) {
        fecha.toDateMillis() ?: LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis.toDateStringOrNull()?.also(onDateSelected)
                onDismiss()
            }) {
                Text(text = "Ok")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancelar") } }
    ) { DatePicker(state = datePickerState) }
}