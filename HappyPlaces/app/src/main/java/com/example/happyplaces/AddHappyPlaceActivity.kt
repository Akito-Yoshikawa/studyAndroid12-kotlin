package com.example.happyplaces

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var etDate: AppCompatEditText? = null
    private var tvAddImage: TextView? = null

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
        Dexter.withContext(this)
            .withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    // nullableじゃないと、falseになる
                    // 写真の読み取り書き込みが許可されているかどうか
                    if(report!!.areAllPermissionsGranted()) {
                        Toast.makeText(this@AddHappyPlaceActivity, "Storage READ/WRITE permission are granted", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken) {

                    showRationalDialogForPermissions()
                    token?.continuePermissionRequest()
                }
        }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("パーミッションがオフになっています")
            .setPositiveButton("GO TO Settings") { _, _ ->
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