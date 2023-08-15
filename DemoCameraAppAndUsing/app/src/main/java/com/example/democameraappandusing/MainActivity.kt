package com.example.democameraappandusing

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.contracts.contract

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamera = findViewById<Button>(R.id.btn_camera)

        btnCamera.setOnClickListener {

            val permission = android.Manifest.permission.CAMERA

            //  カメラの許可の確認
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // 許可されなかった。
                    showRationalDialogForPermissions()

                } else {
                    // パーミッションリクエストダイアログを開く
                    requestPermissionResult.launch(arrayOf(permission))

                }
            } else {

                // カメラを起動する
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                launcher.launch(intent)
            }
        }
    }

    private val requestPermissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->

        if (granted[android.Manifest.permission.CAMERA] == true) {

            // パーミッションが許可されているため、カメラを起動
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            launcher.launch(intent)
        } else {
            // パーミッションが得られなかった時
            showRationalDialogForPermissions()
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == RESULT_OK && result.data != null) {

            // 撮影した画像を取得し反映する
            val thumbNail: Bitmap = result.data!!.extras!!.get("data") as Bitmap

            val ivImage = findViewById<ImageView>(R.id.iv_image)
            ivImage.setImageBitmap(thumbNail)
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
}