package com.example.workouttracker.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.ui.components.MonthHeader
import com.example.workouttracker.ui.components.RetroButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    exercisesByMonth: Map<YearMonth, Map<LocalDate, List<Exercise>>>,
    expandedMonths: List<YearMonth>,
    onToggleMonth: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDeleteWorkout: (LocalDate) -> Unit,
    onEditWorkoutDate: (LocalDate) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "WORKOUT LOG",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            exercisesByMonth
                .keys
                .sortedDescending()
                .forEach { month ->
                    item {
                        MonthHeader(
                            month = month,
                            isExpanded = expandedMonths.contains(month),
                            onToggle = { onToggleMonth(month) }
                        )
                    }
                    if (expandedMonths.contains(month)) {
                        val datesInMonth = exercisesByMonth[month] ?: emptyMap()
                        items(datesInMonth.keys.sortedDescending()) { date ->
                            RetroButton(
                                onClick = { onDateSelected(date) },
                                onEdit = { onEditWorkoutDate(date) },
                                onDelete = { onDeleteWorkout(date) },
                                text = "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)} | ${datesInMonth[date]?.size ?: 0} exercises"
                            )
                        }
                    }
                }
        }
    }
}