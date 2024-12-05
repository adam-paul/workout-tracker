package com.example.workouttracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.workouttracker.data.dao.ExerciseDao
import com.example.workouttracker.data.dao.ExerciseSetDao
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.model.ExerciseSet

@Database(
    entities = [Exercise::class, ExerciseSet::class],
    version = 5
)
abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseSetDao(): ExerciseSetDao

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

                db.execSQL("""
                    INSERT INTO `exercise_new` (`id`, `date`, `name`, `weight`, `reps_or_duration`, `order`)
                    SELECT `id`, `date`, `name`, `weight`, `reps_or_duration`, `order` FROM `exercise`
                """.trimIndent())

                db.execSQL("DROP TABLE `exercise`")
                db.execSQL("ALTER TABLE `exercise_new` RENAME TO `exercise`")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercise ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create exercise_set table with exact specifications
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS `exercise_set` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `exercise_id` INTEGER NOT NULL,
                `weight` TEXT NOT NULL,
                `reps_or_duration` TEXT NOT NULL,
                `notes` TEXT NOT NULL DEFAULT '',
                `order` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (`exercise_id`) REFERENCES `Exercise`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())

                // 2. Create index on exercise_id
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_set_exercise_id` ON `exercise_set` (`exercise_id`)")

                // 3. Copy existing exercise data into sets
                db.execSQL("""
            INSERT INTO `exercise_set` (`exercise_id`, `weight`, `reps_or_duration`, `notes`, `order`)
            SELECT `id`, `weight`, `reps_or_duration`, `notes`, 0
            FROM `exercise`
        """.trimIndent())

                // 4. Create temporary exercise table with new structure
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS `exercise_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `date` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `order` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

                // 5. Copy exercise data to new table
                db.execSQL("""
            INSERT INTO `exercise_new` (`id`, `date`, `name`, `order`)
            SELECT `id`, `date`, `name`, `order`
            FROM `exercise`
        """.trimIndent())

                // 6. Drop old table and rename new one
                db.execSQL("DROP TABLE `exercise`")
                db.execSQL("ALTER TABLE `exercise_new` RENAME TO `exercise`")
            }
        }

        fun getDatabase(context: Context): ExerciseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    "exercise_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}