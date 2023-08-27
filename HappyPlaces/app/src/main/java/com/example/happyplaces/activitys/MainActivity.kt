package com.example.happyplaces.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var happyPlaceListView: RecyclerView? = null
    private var tvNoRecrdsAvailable: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        happyPlaceListView = findViewById(R.id.rv_happy_places_list)
        tvNoRecrdsAvailable = findViewById(R.id.tv_no_records_available)

        val fabAddHappyPlace:
                FloatingActionButton = findViewById(R.id.fabAddHappyPlace)
        fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }

        getHappyPlacesListFromLocalDB()
    }

    private fun setupHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>) {
        happyPlaceListView?.layoutManager = LinearLayoutManager(this)
        happyPlaceListView?.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(happyPlaceList)
        happyPlaceListView?.adapter = placesAdapter
    }

    private fun getHappyPlacesListFromLocalDB() {

        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList = dbHandler.getHappyPlacesList()

        if (getHappyPlaceList.size > 0) {
            // リサイクルビューを表示
            happyPlaceListView?.visibility = View.VISIBLE

            tvNoRecrdsAvailable?.visibility = View.GONE

            setupHappyPlacesRecyclerView(getHappyPlaceList)
        } else {
            // リサイクルビューを非表示
            happyPlaceListView?.visibility = View.GONE
            tvNoRecrdsAvailable?.visibility = View.VISIBLE
        }
    }
}