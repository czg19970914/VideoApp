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
        const val TAG = "VideoPlayerView"
        
        // 长按超过多少毫秒才触发手势的阈值
        const val LONG_PRESSED_THRESHOLD = 150L
        // 长按后再经过一段时间间隔来判断用户的意图
        const val GET_TYPE_TIME = LONG_PRESSED_THRESHOLD + 300L

        // 真机上MOVE触发敏感，扩大触发范围阈值
        const val IS_MOVE_THRESHOLD = 5

        // 单击事件中最晚抬起事件
        const val SINGLE_CLICK_DURATION = 100L
        // 双击事件中点击最长事件间隔
        const val DOUBLE_CLICK_INTERVAL = 250L

        // 触发手势的几种类型
        const val GESTURE_TYPE_ERROR = 0
        const val ADJUST_LIGHT = 1
        const val ADJUST_VOLUME  = 2
        const val ADJUST_VIDEO_TIME = 3
        const val VIDEO_FAST_FORWARD = 4
        // 新增单击以及双击事件
        const val VIDEO_SINGLE_CLICK = 5
        const val VIDEO_DOUBLE_CLICK = 6
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    private var mScreenWidth = resources.displayMetrics.widthPixels
    private var mFirstTouchTime: Long? = null
    private var mFirstTouchX: Float? = null
    private var mFirstTouchY: Float? = null

    // 双击事件适配属性
    private var mLastTouchTime: Long? = null
    private var mClickCount: Int = 0

    private var mVideoGestureListener : VideoGestureListener? = null

    // 设置一个flag，看是否一开始没到触发时间阈值，从而取消对应的动作
    private var mCancelGesture: Boolean = false

    // 设置一个flag，是否可以使用手势
    private var mCanUseGesture: Boolean = false

    // 设置一个flag，只在手势操作开始时做一些操作
    private var mStartGesture: Boolean = true

    // 当前手势类型，默认是error
    private var mCurrentGestureType: Int = GESTURE_TYPE_ERROR

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

                mCancelGesture = false
                mCanUseGesture = false
                mStartGesture = true
                mCurrentGestureType = GESTURE_TYPE_ERROR
            }
            MotionEvent.ACTION_MOVE -> {
                if (mCanUseGesture) {
                    if (
                        mFirstTouchTime != null
                        && System.currentTimeMillis() - mFirstTouchTime!! > GET_TYPE_TIME
                    ) {
                        if (mStartGesture) {
                            mCurrentGestureType = getGestureYpe(event.x, event.y)
                            mVideoGestureListener?.gestureStart(mCurrentGestureType)
                            mStartGesture = false
                        }
                        Log.d(TAG, "onTouchEvent: mCurrentGestureType -> $mCurrentGestureType")
                        when (mCurrentGestureType) {
                            ADJUST_LIGHT -> {
                                if (mVideoGestureListener != null && mFirstTouchY != null) {
                                    mVideoGestureListener!!.adjustLight(mFirstTouchY!!, event.y)
                                }
                            }
                            ADJUST_VOLUME -> {
                                if (mVideoGestureListener != null && mFirstTouchY != null) {
                                    mVideoGestureListener!!.adjustVolume(mFirstTouchY!!, event.y)
                                }
                            }
                            ADJUST_VIDEO_TIME -> {
                                if (mVideoGestureListener != null && mFirstTouchX != null) {
                                    mVideoGestureListener!!.adjustVideoTime(mFirstTouchX!!, event.x)
                                }
                            }
                        }
                    }
                } else if (!mCancelGesture) {
                    if(isMove(event.x, event.y)) {
                        if (
                            mFirstTouchTime != null &&
                            System.currentTimeMillis() - mFirstTouchTime!! > LONG_PRESSED_THRESHOLD
                        ) {
                            mCanUseGesture = true
                        } else {
                            Log.d(TAG, "onTouchEvent: cancel gesture!")
                            mCancelGesture = true
                            mCanUseGesture = false
                            mStartGesture = false
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!mCanUseGesture && mCurrentGestureType == GESTURE_TYPE_ERROR
                    && System.currentTimeMillis() - mFirstTouchTime!! < SINGLE_CLICK_DURATION) {
                    mClickCount++
                    mCurrentGestureType = VIDEO_SINGLE_CLICK
                    mVideoGestureListener?.videoSingleClick()
                    if (mClickCount == 2) {
                        if ((mLastTouchTime != null) && mFirstTouchTime!! - mLastTouchTime!! < DOUBLE_CLICK_INTERVAL) {
                            mCurrentGestureType = VIDEO_DOUBLE_CLICK
                            mVideoGestureListener?.videoDoodleClick()
                            mClickCount = 0
                        } else {
                            mClickCount = 1
                        }
                    }
                } else {
                    mClickCount = 0
                }
                mCancelGesture = false
                mCanUseGesture = false
                mStartGesture = true

                mVideoGestureListener?.gestureFinish(mCurrentGestureType)
                mLastTouchTime = mFirstTouchTime
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!mCanUseGesture && mCurrentGestureType == GESTURE_TYPE_ERROR
                    && System.currentTimeMillis() - mFirstTouchTime!! < SINGLE_CLICK_DURATION) {
                    mClickCount++
                    mCurrentGestureType = VIDEO_SINGLE_CLICK
                    mVideoGestureListener?.videoSingleClick()
                    if (mClickCount == 2) {
                        if ((mLastTouchTime != null) && mFirstTouchTime!! - mLastTouchTime!! < DOUBLE_CLICK_INTERVAL) {
                            mCurrentGestureType = VIDEO_DOUBLE_CLICK
                            mVideoGestureListener?.videoDoodleClick()
                            mClickCount = 0
                        } else {
                            mClickCount = 1
                        }
                    }
                } else {
                    mClickCount = 0
                }
                mCancelGesture = false
                mCanUseGesture = false
                mStartGesture = true

                mVideoGestureListener?.gestureFinish(mCurrentGestureType)
                mLastTouchTime = mFirstTouchTime
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

    private fun getGestureYpe(x: Float, y: Float) : Int {
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
        } else if (mFirstTouchX != null) {
            return if (mFirstTouchX!! < mScreenWidth / 2) {
                ADJUST_LIGHT
            } else {
                ADJUST_VOLUME
            }
        }
        return GESTURE_TYPE_ERROR
    }

    fun setVideoGestureListener(videoGestureListener: VideoGestureListener) {
        mVideoGestureListener = videoGestureListener
    }

    // 回调接口，用来窗口播放界面以及视频
    interface VideoGestureListener {
        fun videoSingleClick()
        fun videoDoodleClick()
        fun gestureStart(gestureType: Int)

        fun adjustLight(startY: Float, currentY: Float)

        fun adjustVolume(startY: Float, currentY: Float)

        fun adjustVideoTime(startX: Float, currentX: Float)

        fun gestureFinish(gestureType: Int)
    }
}

