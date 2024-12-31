package com.example.videoapp.views.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.example.videoapp.R

/**
 * 自定义ImageView
 * 根据view的宽度( or 高度)以及自定义属性比例(whRatio)来确定imageView的大小
 */
class CustomImageView(
    context: Context,
    attrs: AttributeSet
): androidx.appcompat.widget.AppCompatImageView(context, attrs) {
    private var widthRatio: Float = 0F
    private var heightRatio: Float = 0F

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomImageView)
        val whRatio = attributes.getString(R.styleable.CustomImageView_whRatio)
        attributes.recycle()
        whRatio?.let {
            val radioList = it.split(":")
            when (radioList.size) {
                2 -> {
                    widthRatio = radioList[0].toFloat()
                    heightRatio = radioList[1].toFloat()
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var wMeasureSpec = widthMeasureSpec
        var hMeasureSpec = heightMeasureSpec
        if (widthRatio != 0F && heightRatio != 0F) {
            val ratio = getRatio()
            val lp: ViewGroup.LayoutParams = layoutParams
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            if (widthMode == MeasureSpec.EXACTLY && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                // 根据宽度和比例计算高度
                hMeasureSpec = MeasureSpec.makeMeasureSpec(
                    (widthSize / ratio).toInt(),
                    MeasureSpec.EXACTLY
                )
            } else if (heightMode == MeasureSpec.EXACTLY && lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                // 根据高度和比例计算高宽度
                wMeasureSpec = MeasureSpec.makeMeasureSpec(
                    (heightSize * ratio).toInt(),
                    MeasureSpec.EXACTLY
                )
            }
        }

        super.onMeasure(wMeasureSpec, hMeasureSpec)
    }

    private fun getRatio(): Float {
        return widthRatio / heightRatio
    }
}