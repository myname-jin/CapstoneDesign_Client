package com.minyook.overnight.ui.FirstScrean // 1. 패키지명 수정

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.minyook.overnight.ui.FirstScrean.OnboardingActivity
import com.minyook.overnight.R // 3. R 파일 경로

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo: ImageView = findViewById(R.id.iv_splash_logo)
        val spinner: ProgressBar = findViewById(R.id.progress_bar)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        logo.visibility = View.VISIBLE
        spinner.visibility = View.VISIBLE
        logo.startAnimation(fadeIn)
        spinner.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            //  OnboardingActivity로 이동
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500)
    }
}