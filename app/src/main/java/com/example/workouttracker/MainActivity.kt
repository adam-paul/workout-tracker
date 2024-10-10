package com.example.workouttracker
import com.example.workouttracker.ui.theme.WorkoutTrackerTheme

import android.os.Bundle
import android.app.DatePickerDialog
import android.widget.DatePicker

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
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
import androidx.room.migration.Migration

import androidx.sqlite.db.SupportSQLiteDatabase

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import org.burnoutcrew.reorderable.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = ExerciseDatabase.getDatabase(applicationContext)
        setContent {
            WorkoutTrackerTheme {
                ExerciseTrackerApp(database)
            }
        }
    }
}

@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "weight") val weight: String,
    @ColumnInfo(name = "reps_or_duration") val repsOrDuration: String,
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "order") val order: Int = 0
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

    @Update
    suspend fun updateExercises(exercises: List<Exercise>)

    @Query("SELECT * FROM exercise WHERE date = :date ORDER BY `order` ASC")
    suspend fun getExercisesByDate(date: String): List<Exercise>
}

@Database(entities = [Exercise::class], version = 4)
abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercise ADD COLUMN `order` INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create new table without the `notes` column
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS `exercise_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `date` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `weight` TEXT NOT NULL,
                `reps_or_duration` TEXT NOT NULL,
                `order` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

                // 2. Copy data from old table to new table
                db.execSQL("""
            INSERT INTO `exercise_new` (`id`, `date`, `name`, `weight`, `reps_or_duration`, `order`)
            SELECT `id`, `date`, `name`, `weight`, `reps_or_duration`, `order` FROM `exercise`
        """.trimIndent())

                // 3. Drop the old table
                db.execSQL("DROP TABLE `exercise`")

                // 4. Rename the new table to the old name
                db.execSQL("ALTER TABLE `exercise_new` RENAME TO `exercise`")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercise ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: android.content.Context): ExerciseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    "exercise_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ExerciseViewModel(private val dao: ExerciseDao) : ViewModel() {
    val exercisesByDate: Flow<Map<LocalDate, List<Exercise>>> = dao.getAllExercises()
        .map { exercises ->
            exercises.groupBy { LocalDate.parse(it.date) }
                .mapValues { (_, exercises) -> exercises.sortedBy { it.order } }
        }

    fun updateWorkoutDate(oldDate: LocalDate, newDate: LocalDate) {
        viewModelScope.launch {
            val exercises = dao.getExercisesByDate(oldDate.toString())
            exercises.forEach { exercise ->
                dao.updateExercise(exercise.copy(date = newDate.toString()))
            }
        }
    }

    fun addExercise(date: LocalDate, name: String, weight: String, repsOrDuration: String, notes: String) {
        viewModelScope.launch {
            val currentExercises = dao.getExercisesByDate(date.toString())
            val maxOrder = currentExercises.maxOfOrNull { it.order } ?: -1

            val exercise = Exercise(
                date = date.toString(),
                name = name,
                weight = weight,
                repsOrDuration = repsOrDuration,
                notes = notes,
                order = maxOrder + 1
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

    fun reorderExercises(exercises: List<Exercise>) {
        viewModelScope.launch {
            dao.updateExercises(exercises)
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
                    },
                    onReorderExercises = { updatedExercises ->
                        viewModel.reorderExercises(updatedExercises)
                    }
                )
            }
            composable("addExercise") {
                AddEditExerciseScreen(
                    onExerciseAdded = { name, weight, repsOrDuration, notes ->
                        viewModel.addExercise(LocalDate.now(), name, weight, repsOrDuration, notes)
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
                    onExerciseAdded = { name, weight, repsOrDuration, notes ->
                        viewModel.addExercise(date, name, weight, repsOrDuration, notes)
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
                        onExerciseAdded = { name, weight, repsOrDuration, notes ->
                            viewModel.updateExercise(
                                exercise.copy(
                                    name = name,
                                    weight = weight,
                                    repsOrDuration = repsOrDuration,
                                    notes = notes
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
                RetroButton(
                    onClick = { onDateSelected(date) },
                    onEdit = { onEditWorkoutDate(date) },
                    onDelete = { onDeleteWorkout(date) },
                    text = "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)} | ${exercisesByDate[date]?.size ?: 0} exercises"
                )
            }
        }
    }
}

@Composable
fun RetroButton(
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    text: String
) {
    var isPressed by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(Color(0xFFD0D0D0))
                .border(1.dp, Color.Black)
                .padding(1.dp)
        ) {
            // Top and left highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RectangleShape
                    )
            )
            
            // Bottom and right shadow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = Color.Gray,
                        shape = RectangleShape
                    )
                    .padding(top = 2.dp, start = 2.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onClick() },
                            onLongPress = {
                                showActions = !showActions
                            }
                        )
                    }
            ) {
                Text(
                    text = text,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset {
                            IntOffset(
                                x = if (isPressed) 1.dp.roundToPx() else 0,
                                y = if (isPressed) 1.dp.roundToPx() else 0
                            )
                        },
                    color = Color.Black,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(
            visible = showActions,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                IconButton(onClick = { 
                    onEdit()
                    showActions = false
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Workout Date")
                }
                IconButton(onClick = { 
                    onDelete()
                    showActions = false
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Workout")
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
    onEditExercise: (Exercise) -> Unit,
    onReorderExercises: (List<Exercise>) -> Unit
) {
    val exerciseList = remember { mutableStateListOf<Exercise>().apply { addAll(exercises) } }

    LaunchedEffect(exercises) {
        val newExerciseIds = exercises.map { it.id }.toSet()
        val currentExerciseIds = exerciseList.map { it.id }.toSet()
        if (newExerciseIds != currentExerciseIds) {
            // Exercises have been added or removed
            exerciseList.clear()
            exerciseList.addAll(exercises)
        }
    }

    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            exerciseList.apply {
                if (to.index < size) {
                    add(to.index, removeAt(from.index))
                }
            }
        },
        onDragEnd = { _, _ ->
            val updatedExercises = exerciseList.mapIndexed { index, exercise ->
                exercise.copy(order = index)
            }
            onReorderExercises(updatedExercises)
        }
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = onBack) {
            Text("Back")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddExercise,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Exercise")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
            items(exerciseList, key = { it.id }) { exercise ->
                ReorderableItem(state, key = exercise.id) { isDragging ->
                    val elevation = if (isDragging) 8.dp else 1.dp
                    val backgroundColor = if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
                    val scale = if (isDragging) 1.03f else 1f

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = backgroundColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Drag Handle
                            if (isDragging) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Drag Handle",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(24.dp)) // Placeholder to align content
                            }

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
    onExerciseAdded: (name: String, weight: String, repsOrDuration: String, notes: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var weight by remember { mutableStateOf(exercise?.weight ?: "") }
    var repsOrDuration by remember { mutableStateOf(exercise?.repsOrDuration ?: "") }
    var notes by remember { mutableStateOf(exercise?.notes ?: "") }

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
        TextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
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
                        onExerciseAdded(name, weight, repsOrDuration, notes)
                    }
                }
            ) {
                Text(if (exercise == null) "Add Exercise" else "Update Exercise")
            }
        }
    }
}