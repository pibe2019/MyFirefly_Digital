package com.example.myfireflydigital.ui.core.componentes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.myfireflydigital.ui.core.util.getCurrentTime
import com.example.myfireflydigital.ui.core.util.toHourAndMinute
import com.example.myfireflydigital.ui.core.util.toTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(hora: String, onTimeSelected: (String) -> Unit, onDismiss: () -> Unit) {

    val initialTime = remember (hora){  hora.toHourAndMinute() ?: getCurrentTime()}
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.toTimeString())
                    onDismiss()
                }
            ){ Text(text = "Ok") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss){ Text(text = "Cancelar") }
        },
        title = { Text(text = "Selecciona la hora") }
    ) {TimePicker(state = timePickerState)}

}