package com.example.videoapp.views.activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.example.videoapp.R
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.interfaces.VideoView
import com.example.videoapp.presenters.VideoPlayerPresenter
import com.example.videoapp.utils.VideoUtils
import com.example.videoapp.views.customviews.VideoPlayerView

class VideoPlayerActivity : AppCompatActivity(), VideoView {
    private val mVideoPlayerView: VideoPlayerView by lazy {
        findViewById(R.id.video_player_view)
    }
    private val mVideoTextureView: TextureView by lazy {
        findViewById(R.id.video_texture_view)
    }
    private val mToolBarGroup: LinearLayout by lazy {
        findViewById(R.id.tool_bar_group)
    }
    private val mVideoSeekBar: SeekBar by lazy {
        findViewById(R.id.video_seek_bar)
    }
    private val mVideoStartTime: TextView by lazy {
        findViewById(R.id.video_start_time)
    }
    private val mVideoEndTime: TextView by lazy {
        findViewById(R.id.video_end_time)
    }
    private val mHorizontalScreenButton: ImageView by lazy {
        findViewById(R.id.horizontal_screen_button)
    }
    private val mVerticalScreenButton: ImageView by lazy {
        findViewById(R.id.vertical_screen_button)
    }
    private val mVideoPlayedImage: ImageView by lazy {
        findViewById(R.id.video_played_image)
    }
    private val mVideoPausedImage: ImageView by lazy {
        findViewById(R.id.video_paused_image)
    }
    private val mFunctionBar: LinearLayout by lazy {
        findViewById(R.id.function_bar)
    }
    private val mVolumeIcon: ImageView by lazy {
        findViewById(R.id.volume_icon)
    }
    private val mLightIcon: ImageView by lazy {
        findViewById(R.id.light_icon)
    }
    private val mFunctionSeekBar: SeekBar by lazy {
        findViewById(R.id.function_seek_bar)
    }
    private val mVideoFunctionBar: LinearLayout by lazy {
        findViewById(R.id.video_function_bar)
    }
    private val mShowSeekTimeTextView : TextView by lazy {
        findViewById(R.id.show_seek_time)
    }
    private val mMultiSpeedPlayBar: LinearLayout by lazy {
        findViewById(R.id.multi_speed_play_bar)
    }

    // 记录一下当前手势操作的类型，现在主要用作校验作用，没什么其它作用
    private var mCurrentGestureType: Int = VideoPlayerView.GESTURE_TYPE_ERROR

    // 调节音量
    private var mAudioManager: AudioManager? = null

    // 调节窗口屏幕亮度
    private var mLayoutParams: WindowManager.LayoutParams? = null

    private var mVideoPlayerPresenter: VideoPresenter = VideoPlayerPresenter()

    init {
        mVideoPlayerPresenter.setView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
        window.decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        (mVideoPlayerPresenter as VideoPlayerPresenter).initMediaPlayer()
        mVideoTextureView.surfaceTextureListener = object: SurfaceTextureListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                (mVideoPlayerPresenter as VideoPlayerPresenter).startMediaPlayer(
                    intent.getStringExtra(VIDEO_URL_INDEX)!!,
                    mVideoTextureView, mVideoSeekBar,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels
                )
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                (mVideoPlayerPresenter as VideoPlayerPresenter).destroyMediaPlayer()
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

            }

        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //变成横屏了
            (mVideoPlayerPresenter as VideoPlayerPresenter).screenOrientationChanged(
                false, mVideoTextureView,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //变成竖屏了
            (mVideoPlayerPresenter as VideoPlayerPresenter).screenOrientationChanged(
                true, mVideoTextureView,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (mVideoPlayerPresenter as VideoPlayerPresenter).destroyMediaPlayer()

        mLayoutParams = null
        mAudioManager = null

        mToolBarGroup.animation?.cancel()

        mVideoPlayerView.onDestroy()
    }

    companion object {
        const val TAG = "VideoPlayerActivity"

        // 传递到播放页中视频url在intent中的索引
        const val VIDEO_URL_INDEX = "video_url"

        @JvmStatic
        fun startVideoPlayerActivity(context: Context, url: String) {
            val intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtra(VIDEO_URL_INDEX, url)

            context.startActivity(intent)
        }
    }

    fun initVideoSeekBar() {
        var startTime = (mVideoPlayerPresenter as VideoPlayerPresenter).getCurrentVideoTime() //获取当前播放的位置
        val endTime = (mVideoPlayerPresenter as VideoPlayerPresenter).getAllVideoTime() //获取总时长
        videoTimeChanged(startTime, endTime)
        mVideoSeekBar.max = endTime!!
        mVideoSeekBar.setOnSeekBarChangeListener(
            object :SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    startTime = (mVideoPlayerPresenter as VideoPlayerPresenter).getCurrentVideoTime()
                    videoTimeChanged(startTime, endTime)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    (mVideoPlayerPresenter as VideoPlayerPresenter).setSeekbarChangingFlag(true)
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    (mVideoPlayerPresenter as VideoPlayerPresenter).setSeekbarChangingFlag(false)

                    if (p0 != null) {
                        (mVideoPlayerPresenter as VideoPlayerPresenter).videoSeekTo(p0.progress)
                        startTime = (mVideoPlayerPresenter as VideoPlayerPresenter).getCurrentVideoTime()
                        videoTimeChanged(startTime, endTime)
                    }
                }

            }
        )
    }

    fun setVideoClick() {
        mVideoPlayerView.setVideoClickListener(
            object: VideoPlayerView.VideoClickListener {
                override fun videoSingleClick() {
                    if(mToolBarGroup.visibility == View.VISIBLE){
                        showOrHideToolbar(false)
                    }else {
                        showOrHideToolbar(true)
                    }
                }

                override fun videoDoubleClick() {
                    (mVideoPlayerPresenter as VideoPlayerPresenter).pauseStateChanged()
                }

                override fun videoLongClick() {
                    (mVideoPlayerPresenter as VideoPlayerPresenter).changePlayerSpeed(2.0f)
                }

                override fun videoLongClickEnd() {
                    (mVideoPlayerPresenter as VideoPlayerPresenter).resetPlayerSpeed()
                }

            }
        )
    }

    fun setVideoGesture() {
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        (mVideoPlayerPresenter as VideoPlayerPresenter).mMaxVolumeValue =
            mAudioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat()
        mLayoutParams = window.attributes

        mVideoPlayerView.setVideoGestureListener(
            object: VideoPlayerView.VideoGestureListener {
                override fun gestureStart(gestureType: Int) {
                    showFunctionBar(gestureType)
                }

                override fun adjustLight(startY: Float, currentY: Float) {
                    if (mCurrentGestureType == VideoPlayerView.ADJUST_LIGHT) {
                        updateFunctionBar(VideoPlayerView.ADJUST_LIGHT, startY, currentY)
                    } else {
                        Log.i(TAG,
                            "adjustLight: current gesture is $mCurrentGestureType and is not match ADJUST_LIGHT !"
                        )
                    }
                }

                override fun adjustVolume(startY: Float, currentY: Float) {
                    if (mCurrentGestureType == VideoPlayerView.ADJUST_VOLUME) {
                        updateFunctionBar(VideoPlayerView.ADJUST_VOLUME, startY, currentY)
                    } else {
                        Log.i(TAG,
                            "adjustVolume: current gesture is $mCurrentGestureType and is not match ADJUST_VOLUME !"
                        )
                    }
                }

                override fun adjustVideoTime(startX: Float, currentX: Float) {
                    if (mCurrentGestureType == VideoPlayerView.ADJUST_VIDEO_TIME) {
                        updateFunctionBar(VideoPlayerView.ADJUST_VIDEO_TIME, startX, currentX)
                    } else {
                        Log.i(TAG,
                            "adjustVolume: current gesture is $mCurrentGestureType and is not match ADJUST_VIDEO_TIME !"
                        )
                    }
                }

                override fun gestureFinish(gestureType: Int) {
                    if (mCurrentGestureType == gestureType) {
                        when (gestureType) {
                            VideoPlayerView.ADJUST_VIDEO_TIME -> {
                                (mVideoPlayerPresenter as VideoPlayerPresenter).seekToGestureTime()
                            }
                        }
                    }
                    closeFunctionBar()
                }

            }
        )
    }

    fun showOrHideMultiSpeedBar(isShow: Boolean) {
        if (isShow) {
            mMultiSpeedPlayBar.visibility = View.VISIBLE
        } else {
            mMultiSpeedPlayBar.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initToolbar() {
        // 屏蔽toolbar上手势事件
        mToolBarGroup.setOnTouchListener { p0, p1 -> true }

        mVideoPlayedImage.setOnClickListener{
            (mVideoPlayerPresenter as VideoPlayerPresenter).pauseStateChanged()
        }
        mVideoPausedImage.setOnClickListener{
            (mVideoPlayerPresenter as VideoPlayerPresenter).pauseStateChanged()
        }

        mHorizontalScreenButton.setOnClickListener {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        mVerticalScreenButton.setOnClickListener {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun showOrHideToolbar(isShow: Boolean) {
        if (isShow) {
            val alphaAnimator =
                ObjectAnimator.ofFloat(mToolBarGroup, "alpha", 0f, 1f)
            val translationYAnimator =
                ObjectAnimator.ofFloat(mToolBarGroup, "translationY", 100f, 0f)
            val toolBarAnimatorSet = AnimatorSet()
            toolBarAnimatorSet.play(alphaAnimator).with(translationYAnimator)
            toolBarAnimatorSet.addListener(
                object : Animator.AnimatorListener{
                    override fun onAnimationStart(p0: Animator) {
                        mToolBarGroup.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(p0: Animator) {
                        mToolBarGroup.visibility = View.VISIBLE

                        toolBarAnimatorSet.cancel()
                    }

                    override fun onAnimationCancel(p0: Animator) {
                        mToolBarGroup.visibility = View.VISIBLE

                        toolBarAnimatorSet.cancel()
                    }

                    override fun onAnimationRepeat(p0: Animator) {

                    }

                }
            )
            toolBarAnimatorSet.setDuration(500)
            toolBarAnimatorSet.start()
        } else {
            val alphaAnimator =
                ObjectAnimator.ofFloat(mToolBarGroup, "alpha", 1f, 0f)
            val translationYAnimator =
                ObjectAnimator.ofFloat(mToolBarGroup, "translationY", 0f, 100f)
            val toolBarAnimatorSet = AnimatorSet()
            toolBarAnimatorSet.play(alphaAnimator).with(translationYAnimator)
            toolBarAnimatorSet.addListener(
                object : Animator.AnimatorListener{
                    override fun onAnimationStart(p0: Animator) {

                    }

                    override fun onAnimationEnd(p0: Animator) {
                        mToolBarGroup.visibility = View.GONE
                    }

                    override fun onAnimationCancel(p0: Animator) {
                        mToolBarGroup.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(p0: Animator) {

                    }

                }
            )
            toolBarAnimatorSet.setDuration(500)
            toolBarAnimatorSet.start()
        }
    }

    private fun videoTimeChanged(startTime: Int?, endTime: Int?) {
        if(startTime == null || endTime == null) {
            return
        }
        mVideoStartTime.text = VideoUtils.calculateTime(startTime / 1000) //开始时间
        mVideoEndTime.text = VideoUtils.calculateTime(endTime / 1000) //总时长
    }

    private fun adjustVideoTimeText(currentTime: Int, endTime: Int): String {
        val currentTimeText = VideoUtils.calculateTime(currentTime / 1000)
        val endTimeText = VideoUtils.calculateTime(endTime / 1000)
        return "$currentTimeText/$endTimeText"
    }

    fun showPauseView(isPaused: Boolean) {
        if(isPaused) {
            mVideoPausedImage.visibility = View.VISIBLE
            mVideoPlayedImage.visibility = View.GONE
        }else {
            mVideoPausedImage.visibility = View.GONE
            mVideoPlayedImage.visibility = View.VISIBLE
        }
    }

    fun showOrientationView(isVertical: Boolean) {
        if(isVertical) {
            mVerticalScreenButton.visibility = View.GONE
            mHorizontalScreenButton.visibility = View.VISIBLE
        }else {
            mVerticalScreenButton.visibility = View.VISIBLE
            mHorizontalScreenButton.visibility = View.GONE
        }
    }

    private fun initFunctionSeekBar(gestureType: Int) {
        when (gestureType) {
            VideoPlayerView.ADJUST_VOLUME -> {
                if ((mVideoPlayerPresenter as VideoPlayerPresenter).mMaxVolumeValue?.toInt() != null) {
                    mFunctionSeekBar.max = (mVideoPlayerPresenter as VideoPlayerPresenter).mMaxVolumeValue?.toInt()!!
                    val currentVolume = mAudioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (currentVolume != null) {
                        (mVideoPlayerPresenter as VideoPlayerPresenter).mVolumeValue = currentVolume.toFloat()
                        mFunctionSeekBar.progress = currentVolume
                    }
                }
            }
            VideoPlayerView.ADJUST_LIGHT -> {
                mFunctionSeekBar.max = 100
                var currentLight = mLayoutParams?.screenBrightness
                if (currentLight == -1f) {
                    currentLight = VideoUtils.getScreenBrightness(baseContext) / 255f
                }
                if (currentLight != null && currentLight >= 0) {
                    (mVideoPlayerPresenter as VideoPlayerPresenter).mLightValue = currentLight
                    mFunctionSeekBar.progress = (currentLight * 100).toInt()
                }
            }
        }
    }

    fun updateFunctionSeekBar(gestureType: Int, progress: Float) {
        when (gestureType) {
            VideoPlayerView.ADJUST_VOLUME -> {
                mFunctionSeekBar.progress = progress.toInt()
                mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, progress.toInt(), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
            }
            VideoPlayerView.ADJUST_LIGHT -> {
                mFunctionSeekBar.progress = (progress * 100).toInt()
                if (mLayoutParams != null) {
                    mLayoutParams?.screenBrightness = progress
                    window.attributes = mLayoutParams
                }
            }
            VideoPlayerView.ADJUST_VIDEO_TIME -> {
                val maxTime = mVideoSeekBar.max
                val currentTime = progress.toInt().coerceAtMost(maxTime)
                val adjustTimeText = adjustVideoTimeText(currentTime, maxTime)
                mShowSeekTimeTextView.text = adjustTimeText
            }
        }
    }

    fun showFunctionBar(gestureType: Int) {
        mCurrentGestureType = gestureType
        when (gestureType) {
            VideoPlayerView.ADJUST_VOLUME -> {
                mVolumeIcon.visibility = View.VISIBLE
                mLightIcon.visibility = View.GONE
                mFunctionBar.visibility = View.VISIBLE
                mVideoFunctionBar.visibility = View.GONE

                initFunctionSeekBar(gestureType)
            }
            VideoPlayerView.ADJUST_LIGHT -> {
                mVolumeIcon.visibility = View.GONE
                mLightIcon.visibility = View.VISIBLE
                mFunctionBar.visibility = View.VISIBLE
                mVideoFunctionBar.visibility = View.GONE

                initFunctionSeekBar(gestureType)
            }
            VideoPlayerView.ADJUST_VIDEO_TIME -> {
                mVolumeIcon.visibility = View.GONE
                mLightIcon.visibility = View.GONE
                mFunctionBar.visibility = View.GONE
                mVideoFunctionBar.visibility = View.VISIBLE
            }
        }
    }



    fun updateFunctionBar(gestureType: Int, startValue: Float, currentValue: Float) {
        (mVideoPlayerPresenter as VideoPlayerPresenter).updateFunctionValue(gestureType, startValue, currentValue)
    }

    fun closeFunctionBar() {
        mCurrentGestureType = VideoPlayerView.GESTURE_TYPE_ERROR
        mVolumeIcon.visibility = View.GONE
        mLightIcon.visibility = View.GONE
        mFunctionBar.visibility = View.GONE
        mVideoFunctionBar.visibility = View.GONE
    }

    fun getVideoViewHeight() : Int {
        return mVideoPlayerView.height
    }

    override fun setPresenter(presenter: VideoPresenter) {
        mVideoPlayerPresenter = presenter
    }
}