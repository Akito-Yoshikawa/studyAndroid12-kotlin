package com.example.a7minutesworkout

import android.app.Application

class ExerciseHistoryApp:Application() {
    val db by lazy {
        ExerciseHistoryDatabase.getInstance(this)
    }
}