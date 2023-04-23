package com.example.a7minutesworkout

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ExerciseHistoryEntity::class], version = 1, exportSchema = false)
abstract class ExerciseHistoryDatabase: RoomDatabase() {

    abstract fun exerciseHistoryDao(): ExerciseHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseHistoryDatabase? = null

        fun getInstance(context: Context): ExerciseHistoryDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ExerciseHistoryDatabase::class.java,
                        "exercise_History_database"
                    ).fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}

