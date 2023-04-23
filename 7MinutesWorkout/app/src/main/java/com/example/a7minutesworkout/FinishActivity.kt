package com.example.a7minutesworkout

import android.icu.util.Calendar
import android.icu.util.LocaleData
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.a7minutesworkout.databinding.ActivityFinishBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class FinishActivity : AppCompatActivity() {

    private var binding: ActivityFinishBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinishBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarFinish)

        val exerciseHistoryDao = (applicationContext as ExerciseHistoryApp).db.exerciseHistoryDao()

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding?.toolbarFinish?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding?.btnFinish?.setOnClickListener {
            addRecord(exerciseHistoryDao)
            finish()
        }
    }

    private fun addRecord(exerciseHistoryDao: ExerciseHistoryDao) {
        // 現在日時取得
//        val date = LocalDateTime.now()
        val calendar = Calendar.getInstance()
        val dateTime = calendar.time
        Log.e("Date: ", "" + dateTime)

        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        val date = sdf.format(dateTime)
        Log.e("Formatted Date: ", "" + date)


        lifecycleScope.launch {
            exerciseHistoryDao.insert(ExerciseHistoryEntity(time = date))
        }
    }
}