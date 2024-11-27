package com.example.workouttracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.ui.components.MonthConnector
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
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
                        val sortedDates = datesInMonth.keys.sortedDescending()

                        sortedDates.forEachIndexed { index, date ->
                            item {
                                Box(modifier = Modifier.height(48.dp)) {  // Container for connector and button
                                    MonthConnector(
                                        isFirstItem = index == 0,
                                        isLastItem = index == sortedDates.lastIndex
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 40.dp)
                                    ) {
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
                        // Add spacing after the last entry
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
        }
    }
}