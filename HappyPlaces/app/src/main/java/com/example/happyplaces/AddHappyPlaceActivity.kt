package com.example.happyplaces

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var etDate: AppCompatEditText? = null
    private var tvAddImage: TextView? = null

    private val PERMISSION_WRITE_EX_STR = 1

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

        // ActionBarをセット
        setSupportActionBar(toolBarPlace)
        // ActionBar戻るボタン追加
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBarPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

            // OKボタンで決定時呼ばれる
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()
        }

        etDate!!.setOnClickListener(this)
        tvAddImage!!.setOnClickListener(this)

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
            val saveImageToInternalStorage = saveImageToInternalStorage(thumbNail)
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
                val saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap!!)
                Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@AddHappyPlaceActivity, "Failed to load the Image from Gallery", Toast.LENGTH_LONG).show()
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