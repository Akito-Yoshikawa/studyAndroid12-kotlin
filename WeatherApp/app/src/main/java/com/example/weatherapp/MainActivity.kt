package com.example.weatherapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!isLocationEnabled()) {
            Toast.makeText(this@MainActivity, "Your location provider is turned off. Please turn off.", Toast.LENGTH_LONG).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {

            // 現在位置情報を取得するパーミッションを追加
            requestSelectCurrentLocation()
        }
    }

    private fun isLocationEnabled(): Boolean {

        val locationManger: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return  locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManger.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }

    private fun requestSelectCurrentLocation() {
        val accessFineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val accessFineLocationPermissionCheck = ContextCompat.checkSelfPermission(this, accessFineLocationPermission)

        val accessCoarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION
        val accessCoarseLocationPermissionCheck = ContextCompat.checkSelfPermission(this, accessCoarseLocationPermission)

        if (accessFineLocationPermissionCheck != PackageManager.PERMISSION_GRANTED && accessCoarseLocationPermissionCheck != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, accessFineLocationPermission)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, accessCoarseLocationPermission)) {
                // パーミッションが必要であることを明示する
                showRationalDialogForPermissions()
            } else {
                // パーミッションリクエストダイアログを開く
                requestSelectCurrentLocationPermissionResult.launch(arrayOf(accessFineLocationPermission, accessCoarseLocationPermission))
            }
        } else {
            // already permitted.
            requestNewLocationData()
        }
    }

    /* 現在位置情報取得パーミッション結果 */
    private val requestSelectCurrentLocationPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { granted ->

        if (granted[android.Manifest.permission.ACCESS_FINE_LOCATION] == true && granted[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // already permitted.
            requestNewLocationData()
        } else {
            showRationalDialogForPermissions()
        }
    }

    /// 現在位置の取得をリクエスト
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMaxUpdates(1)
            .build()
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
    }

    /// 現在位置の取得コールバッグ
    private val mLocationCallBack = object : LocationCallback() {

        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            val mLastLocation: Location = p0.lastLocation!!
//            mLatitude = mLastLocation.latitude
//            mLongitude = mLastLocation.longitude
//
//            Log.i("current latitude", "$mLatitude")
//            Log.i("current longitude", "$mLongitude")

            getLocationWeatherDetails()
        }
    }


    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("パーミッションがオフになっています。設定画面で、パーミッションを許可してください。")
            .setPositiveButton("設定画面へ移動する") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun getLocationWeatherDetails() {

        if (Constants.insNetworkAvailable(this)) {

            Toast.makeText(this@MainActivity, "ネットワークに接続されています", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@MainActivity, "ネットワークに接続されていない", Toast.LENGTH_LONG).show()

        }
   }

}