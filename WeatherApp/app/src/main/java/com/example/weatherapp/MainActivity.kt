package com.example.weatherapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.WeatherService
import com.google.android.gms.location.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private lateinit var customProgressDialog: Dialog

    private lateinit var binding: ActivityMainBinding

    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)

        setupUI()

        if (!isLocationEnabled()) {
            Toast.makeText(this@MainActivity, "Your location provider is turned off. Please turn off.", Toast.LENGTH_LONG).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {

            // 現在位置情報を取得するパーミッションを追加
            requestSelectCurrentLocation()
        }
    }

    // menuを作成
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    // menuのアイテムを選択した時、呼ばれる
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId) {
            R.id.action_refresh -> {
                requestNewLocationData()
                true
            } else -> super.onOptionsItemSelected(item)
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
            val mLatitude = mLastLocation.latitude
            val mLongitude = mLastLocation.longitude

            getLocationWeatherDetails(mLatitude, mLongitude)
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

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {

        // ネットワークが利用可能かどうかを確認
        if (Constants.insNetworkAvailable(this)) {

            // Retrofitを初期化して設定
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)// ベースURLを設定
                .addConverterFactory(GsonConverterFactory.create())// JSONデータの変換方法を指定
                .build()

            // WeatherServiceを作成
            val service: WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)

            // 天気情報の取得リクエストを作成
            val listCall: Call<WeatherResponse> = service.getWeather(
                latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
            )

            showProgressDialog()

            // リクエストを非同期で実行し、応答を処理
            listCall.enqueue(object : Callback<WeatherResponse> {
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    // リクエストが失敗した場合の処理
                    Log.e("Error", t!!.message.toString())

                    cancelProgressDialog()
                }

                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {

                        val weatherList: WeatherResponse? = response.body()

                        Log.i("Response Result", "$weatherList")

                        if (weatherList != null) {

                            // 取得したjsonStringを保存
                            val weatherResponseJsonString = Gson().toJson(weatherList)
                            val editor = mSharedPreferences.edit()
                            editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                            editor.apply()

                            setupUI()
                        }

                    } else {

                        when(response.code()) {
                            400 -> {
                                Log.e("Error 400", "Bad Connection")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }

                    }

                    cancelProgressDialog()
                }
            })

        } else {
            Toast.makeText(this@MainActivity, "ネットワークに接続されていない", Toast.LENGTH_LONG).show()
        }
   }

    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)
        customProgressDialog.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog.show()
    }

    private fun cancelProgressDialog() {
        customProgressDialog.dismiss()
    }
    private fun setupUI() {

        // 保持しておいた、jsonStringを取得
        val weatherResponseJsonString = mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")

        if (!weatherResponseJsonString.isNullOrEmpty()) {

            val weatherResponse = Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)

            for (i in weatherResponse.weather.indices) {

                Log.i("Weather Name", weatherResponse.weather.toString())

                // 取得したレスポンスをUIに反映していく
                binding.tvMain.text = weatherResponse.weather[i].main
                binding.tvMainDescription.text = weatherResponse.weather[i].description
                binding.tvTemp.text = weatherResponse.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())

                binding.tvHumidity.text = weatherResponse.main.humidity.toString() + "per cent"
                binding.tvMin.text = weatherResponse.main.temp_min.toString() + " min"
                binding.tvMax.text = weatherResponse.main.temp_max.toString() + " max"
                binding.tvSpeed.text = weatherResponse.wind.speed.toString()
                binding.tvName.text = weatherResponse.name
                binding.tvCountry.text = weatherResponse.sys.country

                binding.tvSunriseTime.text = unixTime(weatherResponse.sys.sunrise)
                binding.tvSunsetTime.text = unixTime(weatherResponse.sys.sunset)

                when (weatherResponse.weather[i].icon) {
                    "01d" -> binding.ivMain.setImageResource(R.drawable.sunny)
                    "02d" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "03d" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "04d" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "04n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "10d" -> binding.ivMain.setImageResource(R.drawable.rain)
                    "11d" -> binding.ivMain.setImageResource(R.drawable.storm)
                    "13d" -> binding.ivMain.setImageResource(R.drawable.snowflake)
                    "01n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "02n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "03n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "10n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "11n" -> binding.ivMain.setImageResource(R.drawable.rain)
                    "13n" -> binding.ivMain.setImageResource(R.drawable.snowflake)
                }
            }

        }
    }

    private fun getUnit(value: String): String? {

        var localValue = "°C"

        if ("US" == value || "LR" == value || "MM" == value) {
            localValue = "°F"
        }

        return localValue
    }

    private fun unixTime(timex: Long): String? {
        val date = Date(timex *1000L)

        val sdf = SimpleDateFormat("HH:mm", Locale.JAPAN)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}