package com.example.happyplaces.activitys

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var etDate: AppCompatEditText? = null
    private var tvAddImage: TextView? = null

    private var etTitle: AppCompatEditText? = null
    private var etDescription: AppCompatEditText? = null
    private var etLocation: AppCompatEditText? = null
    private var ivPlaceImage: ImageView? = null
    private var tvSelectCurrentLocation: TextView? = null

    private var mHappyPlaceDetails: HappyPlaceModel? = null

    private val permissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        val toolBarPlace: Toolbar = findViewById(R.id.toolbar_add_place)
        etDate = findViewById(R.id.et_date)
        tvAddImage = findViewById(R.id.tv_add_image)

        val btnSave = findViewById<Button>(R.id.btn_save)

        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        etLocation = findViewById(R.id.et_location)
        ivPlaceImage = findViewById(R.id.iv_place_image)

        tvSelectCurrentLocation = findViewById(R.id.tv_select_current_location)

        // ActionBarをセット
        setSupportActionBar(toolBarPlace)
        // ActionBar戻るボタン追加
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBarPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        if(!Places.isInitialized()) {
            Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.google_maps_api_key))
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            val userData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS, HappyPlaceModel::class.java)
            } else {
                intent.getParcelableExtra<HappyPlaceModel>(MainActivity.EXTRA_PLACE_DETAILS)
            }
            mHappyPlaceDetails = userData
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

            // OKボタンで決定時呼ばれる
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()
        }
        updateDateInView()

        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"

            etTitle?.setText(mHappyPlaceDetails!!.title)
            etDescription?.setText(mHappyPlaceDetails!!.description)
            etDate?.setText(mHappyPlaceDetails!!.date)
            etLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

            ivPlaceImage?.setImageURI(saveImageToInternalStorage)

            btnSave.text = "UPDATE"
        }

        etDate!!.setOnClickListener(this)
        tvAddImage!!.setOnClickListener(this)
        btnSave!!.setOnClickListener(this)
        etLocation!!.setOnClickListener(this)
        tvSelectCurrentLocation!!.setOnClickListener(this)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /// onClickイベント(setOnClickListenerしたもの)
    override fun onClick(v: View?) {
        when(v!!.id) {
            // 日付選択
            R.id.et_date -> {
                DatePickerDialog(this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH))
                    .show()
            }
            // 画像選択
            R.id.tv_add_image -> {

                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when(which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

            // 保存を押下
            R.id.btn_save -> {
                // 入力内容nullチェック

                when {
                    etTitle?.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter title", Toast.LENGTH_LONG).show()
                    }

                    etDescription?.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter description", Toast.LENGTH_LONG).show()
                    }

                    etLocation?.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter location", Toast.LENGTH_LONG).show()
                    }

                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please select an image", Toast.LENGTH_LONG).show()

                    } else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                            etTitle?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            etDescription?.text.toString(),
                            etDate?.text.toString(),
                            etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )

                        val dbHandler = DatabaseHandler(this)

                        if (mHappyPlaceDetails == null) {

                            val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                            if (addHappyPlace > 0) {
                                setResult(Activity.RESULT_OK)

                                finish()
                            }
                        } else {
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)

                            if (updateHappyPlace > 0) {
                                setResult(Activity.RESULT_OK)

                                finish()
                            }


                        }

                    }


                }
            }

            // 位置情報
            R.id.et_location -> {

                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this)

                autocompleteLauncher.launch(intent)
            }

            // 現在位置情報
            R.id.tv_select_current_location -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(this@AddHappyPlaceActivity, "Your location provider is turned off. Please turn off.", Toast.LENGTH_LONG).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {

                    // 現在位置情報を取得するパーミッションを追加
                    requestSelectCurrentLocation()
                }
            }
        }
    }

    /* カメラのパーミッションを要求する */
    private fun takePhotoFromCamera() {

        val permission = android.Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // パーミッションが必要であることを明示する
                showRationalDialogForPermissions()
            } else {
                // パーミッションリクエストダイアログを開く
                requestPermissionCamera.launch(permission)
            }

        } else {
            // パーミッションが許可されているため、カメラを起動
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }

    /* カメラパーミッション要求結果  */
    private val requestPermissionCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

        if (granted) {

            // パーミッションが許可されているため、カメラを起動
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } else {
            // パーミッションが得られなかった時
            showRationalDialogForPermissions()
        }
    }

    /* カメラ撮影後処理 */
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == RESULT_OK && result.data != null) {

            // 撮影した画像を取得し反映する
            val thumbNail: Bitmap = result.data!!.extras!!.get("data") as Bitmap

            val ivPlaceImage = findViewById<ImageView>(R.id.iv_place_image)
            ivPlaceImage.setImageBitmap(thumbNail)

            // 画像ファイルの保存
            saveImageToInternalStorage = saveImageToInternalStorage(thumbNail)
            Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")


        }
    }

    /* 画像ファイル読み取りパーミッションを要求する */
    private fun choosePhotoFromGallery() {

        val permission = if (Build.VERSION.SDK_INT > 32) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // パーミッションが必要であることを明示する
                showRationalDialogForPermissions()
            } else {
                // パーミッションリクエストダイアログを開く
                requestPermissionResult.launch(arrayOf(permission))
            }
        } else {
            // already permitted.
            actionPickLauncher.launch("image/*")
        }
    }

    /* 画像ファイル読み取りパーミッション結果 */
    private val requestPermissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->

        if ((Build.VERSION.SDK_INT > 32 && granted[android.Manifest.permission.READ_MEDIA_IMAGES] == true)
            || (Build.VERSION.SDK_INT <= 32 && granted[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true)) {

            actionPickLauncher.launch("image/*")
        } else {
            showRationalDialogForPermissions()
        }
    }

    /* 画像ファイル選択後処理 */
    private val actionPickLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { result ->

        result?.let {
            try {
                val selectedImageBitmap = it.getBitmapOrNull(this.contentResolver)
                val ivPlaceImage = findViewById<ImageView>(R.id.iv_place_image)
                ivPlaceImage.setImageBitmap(selectedImageBitmap)

                // 画像ファイルの保存
                saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap!!)
                Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@AddHappyPlaceActivity, "Failed to load the Image from Gallery", Toast.LENGTH_LONG).show()
            }
        }
    }

    /* 現在位置情報取得パーミッションを要求する */
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
            Toast.makeText(this@AddHappyPlaceActivity, "SelectCurrentLocation already permitted.", Toast.LENGTH_LONG).show()
        }
    }

    /* 現在位置情報取得パーミッション結果 */
    private val requestSelectCurrentLocationPermissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->

        if (granted[android.Manifest.permission.ACCESS_FINE_LOCATION] == true && granted[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // already permitted.
            Toast.makeText(this@AddHappyPlaceActivity, "SelectCurrentLocation already permitted.", Toast.LENGTH_LONG).show()
        } else {
            showRationalDialogForPermissions()
        }
    }

    /* google map API 取得状態 */
    private val autocompleteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        when (result.resultCode) {

            RESULT_OK -> {

                // Handle a successful result
                val data = result.data

                if (data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)

                    etLocation?.setText(place.address)
                    mLatitude = place.latLng!!.latitude
                    mLongitude = place.latLng!!.longitude
                }
            }

            AutocompleteActivity.RESULT_ERROR -> {
                Toast.makeText(this@AddHappyPlaceActivity, "RESULT ERROR", Toast.LENGTH_LONG).show()
            }

            RESULT_CANCELED -> {
                Toast.makeText(this@AddHappyPlaceActivity, "RESULT CANCELED", Toast.LENGTH_LONG).show()
            }
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
            }
            .setNegativeButton("Cancel") {dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView() {
        val myFormat = "yyyy.MM.dd"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        etDate!!.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("HappyPlacesImages", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            stream.flush()
            stream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

}

/* Bitmap拡張関数 */
fun Uri.getBitmapOrNull(contentResolver: ContentResolver): Bitmap? {
    return kotlin.runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(contentResolver, this)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, this)
        }
    }.getOrNull()
}