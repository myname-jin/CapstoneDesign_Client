package com.minyook.overnight.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.minyook.overnight.R
import com.minyook.overnight.databinding.ActivityMainBinding

class OvernightActivity : AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overnight)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // activity_main.xml 레이아웃을 화면에 연결합니다.
        setContentView(R.layout.activity_overnight)
    }
}