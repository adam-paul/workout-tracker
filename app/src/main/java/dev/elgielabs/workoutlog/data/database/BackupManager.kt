package dev.elgielabs.workoutlog.data.database

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import androidx.activity.result.ActivityResultLauncher
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {
    private var backupLauncher: ActivityResultLauncher<Intent>? = null
    private var restoreLauncher: ActivityResultLauncher<Intent>? = null

    fun registerLaunchers(
        backupLauncher: ActivityResultLauncher<Intent>,
        restoreLauncher: ActivityResultLauncher<Intent>
    ) {
        this.backupLauncher = backupLauncher
        this.restoreLauncher = restoreLauncher
    }

    fun initiateBackup() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            putExtra(Intent.EXTRA_TITLE, "workout_backup_$timestamp.db")
        }
        backupLauncher?.launch(intent)
    }

    fun initiateRestore() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
        }
        restoreLauncher?.launch(intent)
    }

    fun performBackup(uri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath("exercise_database")
            context.contentResolver.openOutputStream(uri)?.use { output ->
                dbFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
            restartApp()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun performRestore(uri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath("exercise_database")

            // Delete any existing WAL files
            context.getDatabasePath("exercise_database-wal").delete()
            context.getDatabasePath("exercise_database-shm").delete()

            context.contentResolver.openInputStream(uri)?.use { input ->
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            restartApp()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun restartApp() {
        // Create a fresh start intent
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        // Kill the current process
        Process.killProcess(Process.myPid())
    }
}