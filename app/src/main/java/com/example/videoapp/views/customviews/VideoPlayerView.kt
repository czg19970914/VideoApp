package com.example.videoapp.views.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * Based on TextureView, we add some hand gestures operation to operate videos
 *
 * 手势：左边上移\下移 -> 调亮度
 *      右边上移\下移 -> 调音量
 *      左右移 -> 前后视频时间调节
 * 点击：轻点 -> 调出视频的工具条
 *      双击 -> 视频的暂停、播放
 *      长按 -> 倍速播放视频
 * */
class VideoPlayerView : ConstraintLayout {
    companion object {
        const val TAG = "VideoPlayerView"
        
        // 长按超过多少毫秒才触发手势的阈值
        const val LONG_PRESSED_THRESHOLD = 50L
        // 长按后再经过一段时间间隔来判断用户的意图
        const val GET_TYPE_TIME = LONG_PRESSED_THRESHOLD + 150L

        // 真机上MOVE触发敏感，扩大触发范围阈值
        const val IS_MOVE_THRESHOLD = 5

        // 单击事件中最晚抬起事件
        const val SINGLE_CLICK_DURATION = 150L
        // 双击事件中点击最长事件间隔
        const val DOUBLE_CLICK_INTERVAL = 150L
        // 单击事件延时触发，需要比双击事件判定时间长
        const val SINGLE_CLICK_DELAY = SINGLE_CLICK_DURATION + DOUBLE_CLICK_INTERVAL + 50L
        // 长按事件触发的所需要的事件阈值
        const val LONG_CLICK_TIME_THRESHOLD = 1000L

        // 触发手势的几种类型
        const val GESTURE_TYPE_ERROR = 0
        const val ADJUST_LIGHT = 1
        const val ADJUST_VOLUME  = 2
        const val ADJUST_VIDEO_TIME = 3
        const val VIDEO_FAST_FORWARD = 4

        // 发送单击消息标签
        const val SINGLE_CLICK_MSG = 1
        // 发送长按开始消息标签
        const val LONG_CLICK_START_MSG = 2
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

    private var mVideoClickListener: VideoClickListener? = null

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

    // 表示当前是长按事件，如果触发长按事件则一直是长按事件直到手指松开
    private var mInLongClick = false

    private var mClickEventHandler: ClickEventHandler? = ClickEventHandler(this)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mClickEventHandler?.removeCallbacksAndMessages(null)

                mFirstTouchTime = System.currentTimeMillis()
                mFirstTouchX = event.x
                mFirstTouchY = event.y
                mScreenWidth = resources.displayMetrics.widthPixels

                resetFlags()
                mCurrentGestureType = GESTURE_TYPE_ERROR

                mClickEventHandler?.sendMessageDelayed(LONG_CLICK_START_MSG, LONG_CLICK_TIME_THRESHOLD)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mInLongClick) {
                    return true
                }
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
                        mClickEventHandler?.removeMessages(LONG_CLICK_START_MSG)

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
                mClickEventHandler?.removeMessages(LONG_CLICK_START_MSG)

                if (isClickEvent(mFirstTouchTime!!, System.currentTimeMillis())) {
                    mClickCount++
                    mClickEventHandler?.sendMessageDelayed(SINGLE_CLICK_MSG, SINGLE_CLICK_DELAY)
                    if (mClickCount == 2) {
                        if (mLastTouchTime != null
                            && mFirstTouchTime!! - mLastTouchTime!! < DOUBLE_CLICK_INTERVAL) {
                            mClickEventHandler?.removeMessages(SINGLE_CLICK_MSG)
                            videoDoubleClick()
                            mClickCount = 0
                        } else {
                            mClickCount = 1
                        }
                    }
                } else {
                    mClickCount = 0
                }
                if (mInLongClick) {
                    videoLongClickEnd()
                }
                resetFlags()

                mVideoGestureListener?.gestureFinish(mCurrentGestureType)
                mLastTouchTime = mFirstTouchTime
            }
            MotionEvent.ACTION_CANCEL -> {
                mClickEventHandler?.removeMessages(LONG_CLICK_START_MSG)
                mClickCount = 0
                if (mInLongClick) {
                    videoLongClickEnd()
                }
                resetFlags()

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

    /**
     * 判断手抬起时，该事件是否是应该点击事件
     */
    private fun isClickEvent(clickTime: Long, endTime: Long) : Boolean {
        val timeInterval = endTime - clickTime
        return !mCanUseGesture
                && mCurrentGestureType == GESTURE_TYPE_ERROR
                && !mCancelGesture && !mInLongClick
                && timeInterval < SINGLE_CLICK_DURATION
    }

    private fun resetFlags() {
        mCancelGesture = false
        mCanUseGesture = false
        mStartGesture = true
    }

    fun videoSingleClick() {
        mVideoClickListener?.videoSingleClick()
    }

    private fun videoDoubleClick() {
        mVideoClickListener?.videoDoubleClick()
    }

    fun videoLongClick() {
        mInLongClick = true
        mVideoClickListener?.videoLongClick()
    }

    private fun videoLongClickEnd() {
        mInLongClick = false
        mVideoClickListener?.videoLongClickEnd()
    }

    fun onDestroy() {
        mClickEventHandler?.removeCallbacksAndMessages(null)
        mClickEventHandler = null

        mVideoClickListener = null
        mVideoGestureListener = null
    }

    fun setVideoGestureListener(videoGestureListener: VideoGestureListener) {
        mVideoGestureListener = videoGestureListener
    }

    fun setVideoClickListener(videoClickListener: VideoClickListener) {
        mVideoClickListener = videoClickListener
    }

    // 回调接口，用来窗口播放界面以及视频的手势
    interface VideoGestureListener {
        fun gestureStart(gestureType: Int)

        fun adjustLight(startY: Float, currentY: Float)

        fun adjustVolume(startY: Float, currentY: Float)

        fun adjustVideoTime(startX: Float, currentX: Float)

        fun gestureFinish(gestureType: Int)
    }

    // 回调接口，原来执行点击事件
    interface VideoClickListener {
        fun videoSingleClick()
        fun videoDoubleClick()
        fun videoLongClick()
        fun videoLongClickEnd()
    }

    class ClickEventHandler(videoPlayerView: VideoPlayerView): Handler(Looper.getMainLooper()) {
        private final var mVideoPlayerView: WeakReference<VideoPlayerView>?  = null
        init {
            mVideoPlayerView = WeakReference<VideoPlayerView>(videoPlayerView)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val videoPlayerView: VideoPlayerView? = mVideoPlayerView?.get()
            when (msg.what) {
                SINGLE_CLICK_MSG -> {
                    videoPlayerView?.videoSingleClick()
                }
                LONG_CLICK_START_MSG -> {
                    videoPlayerView?.videoLongClick()
                }
            }
        }

        fun sendMessage(msgFlag: Int) {
            val msg = Message()
            msg.what = msgFlag
            this.sendMessage(msg)
        }

        fun sendMessageDelayed(msgFlag: Int, delayMillis: Long) {
            val msg = Message()
            msg.what = msgFlag
            this.sendMessageDelayed(msg, delayMillis)
        }
    }
}

