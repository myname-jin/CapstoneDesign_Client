package com.minyook.overnight.ui.FirstScrean

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.minyook.overnight.R
import com.minyook.overnight.ui.mainscrean.OvernightActivity // ⚠️ 메인 액티비티 경로
import com.minyook.test1.ui.FirstScrean.OnboardingAdapter

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnSkip: Button
    private lateinit var btnNextStart: Button

    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.view_pager)
        btnSkip = findViewById(R.id.btn_skip)
        btnNextStart = findViewById(R.id.btn_next_start)

        adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        // 페이지가 바뀔 때마다 버튼 상태를 업데이트
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonState(position)
            }
        })

        // '넘기기' 버튼 클릭 시 메인으로
        btnSkip.setOnClickListener {
            navigateToMain()
        }

        // '다음' 또는 '시작하기' 버튼 클릭
        btnNextStart.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                // '다음'
                viewPager.currentItem += 1
            } else {
                // '시작하기'
                navigateToMain()
            }
        }

        updateButtonState(0) // 맨 처음 버튼 상태 설정
    }

    // 페이지 위치에 따라 버튼 텍스트와 보이기 여부 변경
    private fun updateButtonState(position: Int) {
        if (position == adapter.itemCount - 1) {
            // 마지막 페이지
            btnNextStart.text = "시작하기"
            btnSkip.visibility = View.INVISIBLE // '넘기기' 숨기기
        } else {
            // 그 외 페이지
            btnNextStart.text = "다음"
            btnSkip.visibility = View.VISIBLE
        }
    }

    // 메인 액티비티로 이동하고 온보딩 화면 종료
    private fun navigateToMain() {
        val intent = Intent(this, OvernightActivity::class.java)
        startActivity(intent)
        finish()
    }
}