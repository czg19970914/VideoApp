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
 * */
class VideoPlayerView : ConstraintLayout {
    companion object {
        // 长按超过多少毫秒才触发调整视频的音量和亮度的阈值
        const val LONG_PRESSED_THRESHOLD = 1500L

        // 真机上MOVE触发敏感，扩大触发范围阈值
        const val IS_MOVE_THRESHOLD = 5
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    private val mScreenWidth = resources.displayMetrics.widthPixels
    private var mFirstTouchTime: Long? = null
    private var mFirstTouchX: Float? = null
    private var mFirstTouchY: Float? = null

    private var mVideoGestureListener : VideoGestureListener? = null

    // 设置一个flag，是否能够调音量和亮度
    private var mCanChangeVolumeOrLight: Boolean = true

    // 设置一个flag，只在Move中触发一次LongPress事件
    private var mCanLongPress: Boolean = true

    // 判断当前横竖屏状态，默认竖屏
    private var mIsVertical = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mFirstTouchTime = System.currentTimeMillis()
                mFirstTouchX = event.x
                mFirstTouchY = event.y
//                Log.d("czg", "onTouchEvent: Down -> ($mFirstTouchX , $mFirstTouchY)")
            }
            MotionEvent.ACTION_MOVE -> {
//                Log.d("czg", "onTouchEvent: Move -> (" + event.x + " , " + event.y +")")
//                Log.d("czg", "onTouchEvent: Move time -> " + (System.currentTimeMillis() - mFirstTouchTime!!))
                if(isMove(event.x, event.y)) {
                    if (mFirstTouchTime != null &&
                        System.currentTimeMillis() - mFirstTouchTime!! > LONG_PRESSED_THRESHOLD &&
                        mCanChangeVolumeOrLight
                    ) {
                        if (mFirstTouchX != null && mFirstTouchX!! < mScreenWidth / 2) {
                            // 左屏的操作
                            if(mCanLongPress) {
                                mVideoGestureListener?.leftLongPress()
                                mCanLongPress = false
                            }
                            mVideoGestureListener?.leftMove()
                            Log.d("czg", "onTouchEvent: operate light!!!")
                        } else if (mFirstTouchX != null && mFirstTouchX!! > mScreenWidth / 2) {
                            // 右屏的操作
                            if(mCanLongPress) {
                                mVideoGestureListener?.rightLongPress()
                                mCanLongPress = false
                            }
                            mVideoGestureListener?.rightMove()
                            Log.d("czg", "onTouchEvent: operate volume!!!")
                        }
                    } else {
                        mCanChangeVolumeOrLight = false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
//                Log.d("czg", "onTouchEvent: Up")
                mCanChangeVolumeOrLight = true
                mCanLongPress = true

                mVideoGestureListener?.gestureUp()
            }
            MotionEvent.ACTION_CANCEL -> {
//                Log.d("czg", "onTouchEvent: Cancel")
                mCanChangeVolumeOrLight = true
                mCanLongPress = true

                mVideoGestureListener?.gestureUp()
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

    fun setVideoGestureListener(videoGestureListener: VideoGestureListener) {
        mVideoGestureListener = videoGestureListener
    }

    // 回调接口，用来窗口播放界面以及视频
    interface VideoGestureListener {
        fun leftLongPress()

        fun rightLongPress()

        fun leftMove()

        fun rightMove()

        fun gestureUp()
    }
}

