package com.example.workouttracker.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.workouttracker.ui.components.RetroButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun EditWorkoutDateScreen(
    oldDate: LocalDate,
    onDateUpdated: (LocalDate) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        set(oldDate.year, oldDate.monthValue - 1, oldDate.dayOfMonth)
    }

    var newDate by remember { mutableStateOf(oldDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val dialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                newDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateUpdated(newDate)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.setOnDismissListener {
            showDatePicker = false
        }

        dialog.show()
        showDatePicker = false  // Reset immediately after showing
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Edit Workout Date",
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selected Date: ${newDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
        )
        Spacer(modifier = Modifier.height(16.dp))

        RetroButton(
            onClick = { showDatePicker = true },
            text = "Edit Date",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RetroButton(
                onClick = onCancel,
                text = "Cancel",
                modifier = Modifier.weight(1f)
            )
        }
    }
}