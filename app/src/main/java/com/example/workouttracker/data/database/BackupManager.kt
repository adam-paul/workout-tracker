package com.example.workouttracker.data.database

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {
    private var createBackupLauncher: ActivityResultLauncher<Intent>? = null
    private var restoreBackupLauncher: ActivityResultLauncher<Intent>? = null
    private var onBackupCreated: ((String?) -> Unit)? = null
    private var onBackupRestored: (() -> Unit)? = null

    fun registerForActivityResult(activity: ComponentActivity) {
        createBackupLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    performBackup(uri)
                }
            } else {
                onBackupCreated?.invoke(null)
            }
        }

        restoreBackupLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    performRestore(uri)
                }
            }
        }
    }

    fun initiateBackup(onComplete: (String?) -> Unit) {
        onBackupCreated = onComplete
        // Ensure database is properly initialized before backup
        val db = ExerciseDatabase.getDatabase(context)
        db.close()

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            putExtra(Intent.EXTRA_TITLE, "workout_backup_$timestamp.db")
        }
        createBackupLauncher?.launch(intent)
    }

    fun initiateRestore(onComplete: () -> Unit) {
        onBackupRestored = onComplete
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("*/*"))
        }
        restoreBackupLauncher?.launch(intent)
    }

    private fun performBackup(uri: Uri) {
        try {
            // Close any existing database connections
            ExerciseDatabase.closeDatabase()

            // Find and verify the database
            val currentDb = context.getDatabasePath("exercise_database")
            if (!currentDb.exists()) {
                Log.e("BackupManager", "Database file does not exist")
                onBackupCreated?.invoke(null)
                return
            }

            // Ensure we're not copying an empty or corrupted database
            if (currentDb.length() == 0L) {
                Log.e("BackupManager", "Database file is empty")
                onBackupCreated?.invoke(null)
                return
            }

            // Create the backup
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                currentDb.inputStream().use { input ->
                    val bytes = input.copyTo(outputStream)
                    Log.d("BackupManager", "Backed up $bytes bytes from ${currentDb.absolutePath}")
                }
            }

            onBackupCreated?.invoke(uri.toString())
        } catch (e: Exception) {
            Log.e("BackupManager", "Backup failed", e)
            onBackupCreated?.invoke(null)
        }
    }

    private fun performRestore(uri: Uri) {
        try {
            // Force close any existing database connections
            ExerciseDatabase.closeDatabase()

            // Get the database path and clean up existing files
            val currentDb = context.getDatabasePath("exercise_database")
            currentDb.parentFile?.mkdirs()

            // Delete all existing database files
            val dbFiles = listOf(
                currentDb,
                File("${currentDb.path}-journal"),
                File("${currentDb.path}-shm"),
                File("${currentDb.path}-wal")
            )
            dbFiles.forEach { it.delete() }

            // Copy the backup file
            context.contentResolver.openInputStream(uri)?.use { input ->
                currentDb.outputStream().use { output ->
                    val bytes = input.copyTo(output)
                    Log.d("BackupManager", "Restored $bytes bytes to ${currentDb.absolutePath}")
                }
            }

            // Set proper permissions
            currentDb.setReadable(true)
            currentDb.setWritable(true)

            // Reinitialize the database
            val db = ExerciseDatabase.getDatabase(context)
            db.close()

            onBackupRestored?.invoke()
        } catch (e: Exception) {
            Log.e("BackupManager", "Restore failed", e)
        }
    }
}