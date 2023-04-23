package com.example.a7minutesworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a7minutesworkout.databinding.ActivityHistoryBinding
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private var binding: ActivityHistoryBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarHistoryActivity)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "History"
        }

        binding?.toolbarHistoryActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // exerciseHistoryDaoインスタンス取得
        val exerciseHistoryDao = (applicationContext as ExerciseHistoryApp).db.exerciseHistoryDao()
        lifecycleScope.launch {
            // 全件取得
            exerciseHistoryDao.fetchAllExerciseHistory().collect {
                val list = ArrayList(it)
                setupListOfDataInfoRecyclerView(list)
            }
        }
    }

    private fun setupListOfDataInfoRecyclerView(exerciseHistoryList: ArrayList<ExerciseHistoryEntity>) {
        if (exerciseHistoryList.isNotEmpty()) {
            val itemAdapter = ItemAdapter(exerciseHistoryList)

            binding?.rvItemList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemList?.adapter = itemAdapter
        } else {
            Toast.makeText(applicationContext, "No Data AVAILABLE", Toast.LENGTH_LONG).show()
        }
    }
}