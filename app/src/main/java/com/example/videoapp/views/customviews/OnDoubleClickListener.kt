package com.example.videoapp.views.customviews

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

class OnDoubleClickListener(doubleClickCallback: DoubleClickCallback) : View.OnTouchListener {
    private var mCount: Int = 0
    private var mFirstTime: Long = 0
    private var mSecondTime: Long = 0

    private val mInterval: Int = 250
    private var mDoubleClickCallback: DoubleClickCallback

    init {
        mDoubleClickCallback = doubleClickCallback
    }

    interface DoubleClickCallback {
        fun onDoubleClick()

        fun onClick()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        if (p1 != null) {
            if (MotionEvent.ACTION_DOWN == p1.action) {
                mDoubleClickCallback.onClick()

                mCount++
                if(mCount == 1){
                    mFirstTime = System.currentTimeMillis()
                }else if(mCount == 2) {
                    mSecondTime = System.currentTimeMillis()
                    if(mSecondTime - mFirstTime < mInterval) {
                        mDoubleClickCallback.onDoubleClick()
                        mCount = 0
                        mFirstTime = 0
                    }else{
                        mFirstTime = mSecondTime
                        mCount = 1
                    }
                    mSecondTime = 0
                }
            }
        }
        return true
    }
}