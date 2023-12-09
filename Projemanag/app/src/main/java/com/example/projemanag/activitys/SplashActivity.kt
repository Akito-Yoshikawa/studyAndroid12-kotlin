package com.example.projemanag.activitys

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import com.example.projemanag.databinding.ActivitySplashBinding
import com.example.projemanag.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val typeFace: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        binding?.tvAppName?.typeface = typeFace

        Handler(Looper.getMainLooper()).postDelayed({

            val currentUserID = FirestoreClass().getCurrentUserId()

            // 既にログイン済みかどうかチェック
            if (currentUserID.isNotEmpty()) {
                // メイン画面に遷移
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // イントロ画面に遷移
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()

        }, 2500)
    }
}