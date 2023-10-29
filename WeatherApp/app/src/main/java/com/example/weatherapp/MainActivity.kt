package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (!isLocationEnabled()) {
            Toast.makeText(this@MainActivity, "Your location provider is turned off. Please turn off.", Toast.LENGTH_LONG).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Toast.makeText(this@MainActivity, "Your location provider is already turned on.", Toast.LENGTH_LONG).show()
        }
    }

    private fun isLocationEnabled(): Boolean {

        val locationManger: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return  locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManger.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }


}