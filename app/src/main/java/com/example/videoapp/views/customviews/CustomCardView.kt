package com.example.videoapp.views.customviews

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.cardview.widget.CardView

/**
 * 自定义的CardView，使点击带有一点动画的回弹效果
 */
class CustomCardView: CardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    /**
     * 按下时回缩，释放时扩回原位置
     */
    @SuppressLint("Recycle")
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(ev == null)
            return super.dispatchTouchEvent(ev)

        when(ev.action){
            MotionEvent.ACTION_DOWN -> {
                // 按下时，CardView回缩动画，然后
                val scaleXAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 0.95f)
                val scaleYAnimator = ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 0.95f)
                val cardViewAnimatorSet = AnimatorSet()
                cardViewAnimatorSet.play(scaleXAnimator).with(scaleYAnimator)
                cardViewAnimatorSet.setDuration(300)
                cardViewAnimatorSet.start()

                val scaleXAnimator2 = ObjectAnimator.ofFloat(this, "scaleX", 0.95f, 1.0f)
                val scaleYAnimator2 = ObjectAnimator.ofFloat(this, "scaleY", 0.95f, 1.0f)
                val cardViewAnimatorSet2 = AnimatorSet()
                cardViewAnimatorSet2.play(scaleXAnimator2).with(scaleYAnimator2)
                cardViewAnimatorSet2.setDuration(300)
                cardViewAnimatorSet2.start()
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
            }
        }

        return super.dispatchTouchEvent(ev)
    }
}