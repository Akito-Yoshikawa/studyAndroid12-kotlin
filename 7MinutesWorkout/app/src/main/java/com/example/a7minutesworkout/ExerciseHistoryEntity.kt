package com.example.a7minutesworkout

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_history-table")
data class ExerciseHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val time: String = ""
)