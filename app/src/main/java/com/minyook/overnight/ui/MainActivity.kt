package com.minyook.overnight.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minyook.overnight.R
import com.minyook.overnight.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // OvernightActivity로  넘어가는 코드
        binding.button.setOnClickListener {
            val intent = Intent(this, OvernightActivity::class.java)
            startActivity(intent)
        }
    }
}