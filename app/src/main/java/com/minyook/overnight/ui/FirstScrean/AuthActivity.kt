package com.minyook.overnight.ui.FirstScrean

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minyook.overnight.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화 및 레이아웃 설정
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // *참고: 여기서는 Navigation Component가 모든 프래그먼트 관리를 처리하므로
        // 별도의 프래그먼트 트랜잭션 코드는 필요 없습니다.
    }
}