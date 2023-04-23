package com.example.a7minutesworkout

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseHistoryDao {

    @Insert
    suspend fun insert(exerciseHistoryEntity: ExerciseHistoryEntity)

    @Query("SELECT * FROM `exercise_history-table`")
    fun fetchAllExerciseHistory(): Flow<List<ExerciseHistoryEntity>>

}