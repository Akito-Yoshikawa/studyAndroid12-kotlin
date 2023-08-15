package com.example.happyplaces

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
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
                        1 -> Toast.makeText(this@AddHappyPlaceActivity, "Camera selection coming soon", Toast.LENGTH_LONG).show()
                    }
                }
                pictureDialog.show()

            }
        }
    }

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
        }

    }



    private val requestPermissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->

        if ((Build.VERSION.SDK_INT > 32 && granted[android.Manifest.permission.READ_MEDIA_IMAGES] == true)
            || (Build.VERSION.SDK_INT <= 32 && granted[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true)) {
            Toast.makeText(this@AddHappyPlaceActivity, "Storage READ permission are granted", Toast.LENGTH_LONG).show()
        } else {
            showRationalDialogForPermissions()
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
}