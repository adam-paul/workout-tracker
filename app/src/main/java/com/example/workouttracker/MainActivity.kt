package com.example.workouttracker

import android.os.Bundle
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = ExerciseDatabase.getDatabase(applicationContext)
        setContent {
            ExerciseTrackerApp(database)
        }
    }
}

@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "weight") val weight: String,
    @ColumnInfo(name = "reps_or_duration") val repsOrDuration: String
)

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise ORDER BY date DESC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("DELETE FROM exercise WHERE date = :date")
    suspend fun deleteExercisesByDate(date: String)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Query("SELECT * FROM exercise WHERE date = :date")
    suspend fun getExercisesByDate(date: String): List<Exercise>
}

@Database(entities = [Exercise::class], version = 1)
abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        fun getDatabase(context: android.content.Context): ExerciseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    "exercise_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ExerciseViewModel(private val dao: ExerciseDao) : ViewModel() {
    val exercisesByDate: Flow<Map<LocalDate, List<Exercise>>> = dao.getAllExercises().map { exercises ->
        exercises.groupBy { LocalDate.parse(it.date) }
    }

    fun updateWorkoutDate(oldDate: LocalDate, newDate: LocalDate) {
        viewModelScope.launch {
            val exercises = dao.getExercisesByDate(oldDate.toString())
            exercises.forEach { exercise ->
                dao.updateExercise(exercise.copy(date = newDate.toString()))
            }
        }
    }

    fun addExercise(date: LocalDate, name: String, weight: String, repsOrDuration: String) {
        viewModelScope.launch {
            val exercise = Exercise(
                date = date.toString(),
                name = name,
                weight = weight,
                repsOrDuration = repsOrDuration
            )
            dao.insertExercise(exercise)
        }
    }

    fun deleteWorkout(date: LocalDate) {
        viewModelScope.launch {
            dao.deleteExercisesByDate(date.toString())
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            dao.deleteExercise(exercise)
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            dao.updateExercise(exercise)
        }
    }
}

class ExerciseViewModelFactory(private val dao: ExerciseDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun ExerciseTrackerApp(database: ExerciseDatabase) {
    val navController = rememberNavController()
    val viewModel: ExerciseViewModel = viewModel(
        factory = ExerciseViewModelFactory(database.exerciseDao())
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addExercise") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Workout")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("main") {
                val exercisesByDate by viewModel.exercisesByDate.collectAsState(initial = emptyMap())
                MainScreen(
                    exercisesByDate = exercisesByDate,
                    onDateSelected = { date ->
                        navController.navigate("dateScreen/${date}")
                    },
                    onDeleteWorkout = { date ->
                        viewModel.deleteWorkout(date)
                    },
                    onEditWorkoutDate = { date ->
                        navController.navigate("editWorkoutDate/${date}")
                    }
                )
            }
            composable("dateScreen/{date}") { backStackEntry ->
                val date = LocalDate.parse(backStackEntry.arguments?.getString("date"))
                val exercisesByDate by viewModel.exercisesByDate.collectAsState(initial = emptyMap())
                DateScreen(
                    date = date,
                    exercises = exercisesByDate[date] ?: emptyList(),
                    onAddExercise = { navController.navigate("addExercise/${date}") },
                    onBack = { navController.popBackStack() },
                    onDeleteExercise = { exercise ->
                        viewModel.deleteExercise(exercise)
                    },
                    onEditExercise = { exercise ->
                        navController.navigate("editExercise/${exercise.id}")
                    }
                )
            }
            composable("addExercise") {
                AddEditExerciseScreen(
                    onExerciseAdded = { name, weight, repsOrDuration ->
                        viewModel.addExercise(LocalDate.now(), name, weight, repsOrDuration)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                "addExercise/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val date = LocalDate.parse(backStackEntry.arguments?.getString("date"))
                AddEditExerciseScreen(
                    onExerciseAdded = { name, weight, repsOrDuration ->
                        viewModel.addExercise(date, name, weight, repsOrDuration)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                "editExercise/{exerciseId}",
                arguments = listOf(navArgument("exerciseId") { type = NavType.IntType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getInt("exerciseId") ?: return@composable
                val exercisesByDate by viewModel.exercisesByDate.collectAsState(initial = emptyMap())
                val exercise = exercisesByDate.values.flatten().find { it.id == exerciseId }
                if (exercise != null) {
                    AddEditExerciseScreen(
                        exercise = exercise,
                        onExerciseAdded = { name, weight, repsOrDuration ->
                            viewModel.updateExercise(
                                exercise.copy(
                                    name = name,
                                    weight = weight,
                                    repsOrDuration = repsOrDuration
                                )
                            )
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }
            composable(
                "editWorkoutDate/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val oldDate = LocalDate.parse(backStackEntry.arguments?.getString("date"))
                EditWorkoutDateScreen(
                    oldDate = oldDate,
                    onDateUpdated = { newDate ->
                        viewModel.updateWorkoutDate(oldDate, newDate)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    exercisesByDate: Map<LocalDate, List<Exercise>>,
    onDateSelected: (LocalDate) -> Unit,
    onDeleteWorkout: (LocalDate) -> Unit,
    onEditWorkoutDate: (LocalDate) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Title
        Text(
            text = "WORKOUT TRACKER",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(exercisesByDate.keys.sortedDescending()) { date ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)} - ${exercisesByDate[date]?.size ?: 0} exercises"
                        )
                    }
                    Row {
                        IconButton(onClick = { onEditWorkoutDate(date) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Workout Date")
                        }
                        IconButton(onClick = { onDeleteWorkout(date) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Workout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateScreen(
    date: LocalDate,
    exercises: List<Exercise>,
    onAddExercise: () -> Unit,
    onBack: () -> Unit,
    onDeleteExercise: (Exercise) -> Unit,
    onEditExercise: (Exercise) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = onBack) {
            Text("Back")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(date.format(DateTimeFormatter.ISO_LOCAL_DATE), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddExercise,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Exercise")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(exercises) { exercise ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.name, style = MaterialTheme.typography.headlineSmall)
                            Text("Weight: ${exercise.weight}")
                            Text("Reps/Duration: ${exercise.repsOrDuration}")
                        }
                        Row {
                            IconButton(onClick = { onEditExercise(exercise) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Exercise")
                            }
                            IconButton(onClick = { onDeleteExercise(exercise) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Exercise")
                            }
                        }
                    }
                }
            }
        }
    }
}

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
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                newDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateUpdated(newDate)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Edit Workout Date", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selected Date: ${newDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select New Date")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun AddEditExerciseScreen(
    exercise: Exercise? = null,
    onExerciseAdded: (name: String, weight: String, repsOrDuration: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var weight by remember { mutableStateOf(exercise?.weight ?: "") }
    var repsOrDuration by remember { mutableStateOf(exercise?.repsOrDuration ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Exercise Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (or N/A)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = repsOrDuration,
            onValueChange = { repsOrDuration = it },
            label = { Text("Reps/Duration (or N/A)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onExerciseAdded(name, weight, repsOrDuration)
                    }
                }
            ) {
                Text(if (exercise == null) "Add Exercise" else "Update Exercise")
            }
        }
    }
}