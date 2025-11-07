package com.minyook.overnight.ui // 패키지 com.minyook.test.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.minyook.overnight.R // R 파일 경로는 com.minyook.test.R
import kotlin.math.sin

class AnimatedWaveView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path1 = Path()
    private val path2 = Path()

    // --- 부드러운 웨이브 속성 ---
    private val waveAmplitude = 60f
    private val waveFrequency = 0.005f // 값을 낮춰서 물결을 넓게
    // --- ---

    private var offset1 = 0f
    private var offset2 = 0f

    private var waveAnimator: ValueAnimator? = null

    init {
        paint1.color = ContextCompat.getColor(context, R.color.light_purple_40)
        paint2.color = ContextCompat.getColor(context, R.color.light_purple_70)
        paint1.style = Paint.Style.FILL
        paint2.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. 뒷쪽 웨이브 (연한색)
        path1.reset()
        path1.moveTo(0f, 0f)
        path1.lineTo(w, 0f)
        path1.lineTo(w, h * 0.7f)

        for (x in w.toInt() downTo 0) {
            val y = (h * 0.7f) + waveAmplitude * sin((x * waveFrequency) + offset1)
            path1.lineTo(x.toFloat(), y.toFloat())
        }
        path1.close()
        canvas.drawPath(path1, paint1)


        // 2. 앞쪽 웨이브 (진한색)
        path2.reset()
        path2.moveTo(0f, 0f)
        path2.lineTo(w, 0f)
        path2.lineTo(w, h * 0.7f)

        for (x in w.toInt() downTo 0) {
            val y = (h * 0.7f) + waveAmplitude * sin((x * waveFrequency * 1.2f) + offset2)
            path2.lineTo(x.toFloat(), y.toFloat())
        }
        path2.close()
        canvas.drawPath(path2, paint2)
    }

    private fun startAnimation() {
        waveAnimator?.cancel()
        waveAnimator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
            // --- 천천히 움직이도록 ---
            duration = 5000
            // --- ---

            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                offset1 = progress
                offset2 = progress * 0.8f + 1f
                invalidate()
            }
            start()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator?.cancel()
    }
}