package com.example.happyplaces

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class AddHappyPlaceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        val toolBarPlace: Toolbar = findViewById(R.id.toolbar_add_place)

        // ActionBarをセット
        setSupportActionBar(toolBarPlace)
        // ActionBar戻るボタン追加
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBarPlace.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}