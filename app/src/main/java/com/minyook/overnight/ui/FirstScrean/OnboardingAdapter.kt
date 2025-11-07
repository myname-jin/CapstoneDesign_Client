package com.minyook.test1.ui.FirstScrean
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    // 1. 여기에 각 페이지의 제목과 설명을 넣습니다.
    private val fragmentData = listOf(
        Pair(
            "공정한 일관된 평가",
            "사전 기준에 따라 AI가 누구에게나 동일하게 채점해요. 평가자의 주관 없이 신뢰도 높은 결과를 제공합니다."
        ),
        Pair(
            "평가 시간 절약",
            "반복되는 발표 평가를 자동화해 교사와 학생 모두 시간을 아낄 수 있어요."
        ),
        Pair(
            "발표력 향상",
            "발표 전 AI로 미리 점검하고 점수를 기반으로 개선 방향을 확인할 수 있어요."
        )
    )

    override fun getItemCount(): Int = fragmentData.size

    override fun createFragment(position: Int): Fragment {
        val (title, description) = fragmentData[position]
        return OnboardingFragment.newInstance(title, description)
    }
}