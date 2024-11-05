package com.example.videoapp.views.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.abs

/**
 * Based on TextureView, we add some hand gestures operation to operate videos
 *
 * 手势：左边上移\下移 -> 调亮度
 *      右边上移\下移 -> 调音量
 *      左右移 -> 前后视频时间调节
 *      长按 -> 快进
 * */
class VideoPlayerView : ConstraintLayout {
    companion object {
        // 长按超过多少毫秒才触发手势的阈值
        const val LONG_PRESSED_THRESHOLD = 500L
        // 长按后再经过一段时间间隔来判断用户的意图
        const val GET_TYPE_TIME = LONG_PRESSED_THRESHOLD + 500L

        // 真机上MOVE触发敏感，扩大触发范围阈值
        const val IS_MOVE_THRESHOLD = 5

        // 触发手势的几种类型
        const val GESTURE_TYPE_ERROR = 0
        const val ADJUST_LIGHT = 1
        const val ADJUST_VOLUME  = 2
        const val ADJUST_VIDEO_TIME = 3
        const val VIDEO_FAST_FORWARD = 4
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    private var mScreenWidth = resources.displayMetrics.widthPixels
    private var mFirstTouchTime: Long? = null
    private var mFirstTouchX: Float? = null
    private var mFirstTouchY: Float? = null

    private var mVideoGestureListener : VideoGestureListener? = null

    // 设置一个flag，是否可以使用手势
    private var mCanUseGesture: Boolean = false

    // 设置一个flag，只在手势操作开始时做一些操作
    private var mStartGesture: Boolean = true

    // 判断当前横竖屏状态，默认竖屏
    private var mIsVertical = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mFirstTouchTime = System.currentTimeMillis()
                mFirstTouchX = event.x
                mFirstTouchY = event.y
                mScreenWidth = resources.displayMetrics.widthPixels
            }
            MotionEvent.ACTION_MOVE -> {
                if(isMove(event.x, event.y)) {
                    if (
                        mFirstTouchTime != null &&
                        System.currentTimeMillis() - mFirstTouchTime!! > LONG_PRESSED_THRESHOLD
                    ) {
                        mCanUseGesture = true
                    }
                }
                if (mCanUseGesture && mFirstTouchTime != null
                    && System.currentTimeMillis() - mFirstTouchTime!! > GET_TYPE_TIME) {

                    if (mStartGesture) {

                        mStartGesture = false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mCanUseGesture = false
                mStartGesture = true

                mVideoGestureListener?.gestureFinish()
            }
            MotionEvent.ACTION_CANCEL -> {
                mCanUseGesture = false
                mStartGesture = true

                mVideoGestureListener?.gestureFinish()
            }
        }
        return true
    }

    private fun isMove(x: Float, y: Float) : Boolean {
        if(mFirstTouchX == null || mFirstTouchY == null) {
            return false
        }
        val offsetX = abs(x - mFirstTouchX!!)
        val offsetY = abs(y - mFirstTouchY!!)
        if(offsetX <= IS_MOVE_THRESHOLD && offsetY <= IS_MOVE_THRESHOLD) {
            return false
        }
        return true
    }

    private fun getGestureYpe() : Int {
        if(mFirstTouchX == null || mFirstTouchY == null) {
            return GESTURE_TYPE_ERROR
        }
        val offsetX = abs(x - mFirstTouchX!!)
        val offsetY = abs(y - mFirstTouchY!!)

        if(offsetX <= IS_MOVE_THRESHOLD && offsetY <= IS_MOVE_THRESHOLD) {
            return VIDEO_FAST_FORWARD
        }

        if (offsetX > offsetY) {
            return ADJUST_VIDEO_TIME
        }
        return GESTURE_TYPE_ERROR
    }

    fun setVideoGestureListener(videoGestureListener: VideoGestureListener) {
        mVideoGestureListener = videoGestureListener
    }

    // 回调接口，用来窗口播放界面以及视频
    interface VideoGestureListener {
        fun leftLongPress()

        fun rightLongPress()

        fun leftMove()

        fun rightMove()

        fun gestureFinish()
    }
}

