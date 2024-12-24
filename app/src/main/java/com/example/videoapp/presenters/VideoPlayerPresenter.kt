package com.example.videoapp.presenters

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.videoapp.interfaces.VideoModel
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.interfaces.VideoView
import com.example.videoapp.views.activities.VideoPlayerActivity
import com.example.videoapp.views.customviews.VideoPlayerView
import java.util.*
import kotlin.math.abs

class VideoPlayerPresenter: VideoPresenter {
    private var mVideoPlayerView: VideoView? = null

    private var mMediaPlayer: MediaPlayer? = null
    private var mSeekBarTimer: Timer? = null
    private var mIsSeekbarChanging = false // 互斥变量，防止进度条和定时器冲突

    // 视频音量值以及设备最大音量值
    var mVolumeValue: Float? = 0f
    var mMaxVolumeValue: Float? = 0f
    // 屏幕亮度值
    var mLightValue: Float? = 1f
    // 手势操作视频时间跳转
    private var mSeekTime: Int? = null

    override fun setModel(model: VideoModel) {
        // 播放器的控制器不需要model层
    }

    override fun setView(view: VideoView) {
        mVideoPlayerView = view
    }

    fun initMediaPlayer() {
        mMediaPlayer = MediaPlayer()
        mSeekBarTimer = Timer()
    }

    fun destroyMediaPlayer() {
        mSeekBarTimer?.cancel()
        mSeekBarTimer = null

        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    @SuppressLint("ClickableViewAccessibility")
    fun startMediaPlayer(url: String, videoTextureView: TextureView, videoSeekBar: SeekBar,
                         widthPixels: Int, heightPixels: Int) {
        mMediaPlayer?.setSurface(Surface(videoTextureView.surfaceTexture))

        // 解决视频拉伸问题
        mMediaPlayer?.setOnVideoSizeChangedListener { p0, p1, p2 ->
            adjustTextureViewSize(videoTextureView, widthPixels, heightPixels, p1, p2)
        }

        val uri = Uri.parse(url)
        mMediaPlayer?.setDataSource((mVideoPlayerView as VideoPlayerActivity), uri)
        mMediaPlayer?.prepareAsync()

        mMediaPlayer?.setOnPreparedListener {
            it.start()
            (mVideoPlayerView as VideoPlayerActivity).initVideoSeekBar()

            (mVideoPlayerView as VideoPlayerActivity).setVideoClick()
            (mVideoPlayerView as VideoPlayerActivity).setVideoGesture()

            // 启动timer
            mSeekBarTimer?.schedule(object :TimerTask(){
                override fun run() {
                    if(!mIsSeekbarChanging){
                        videoSeekBar.progress = mMediaPlayer!!.currentPosition
                    }
                }
            }, 0, 50)
        }
        mMediaPlayer?.setOnCompletionListener {
            it.seekTo(0)
            (mVideoPlayerView as VideoPlayerActivity).showPauseView(true)
        }
    }

    private fun adjustTextureViewSize(textureView: TextureView,
                                      viewWidth: Int, viewHeight: Int,
                                      videoWidth: Int, videoHeight: Int) {
        val layoutParams: ConstraintLayout.LayoutParams =
            textureView.layoutParams as ConstraintLayout.LayoutParams

        val sx: Float = viewWidth.toFloat() / videoWidth.toFloat()
        val sy: Float = viewHeight.toFloat() / videoHeight.toFloat()

        if(sx >= sy) {
            layoutParams.width = (videoWidth * sy).toInt()
            layoutParams.height = (videoHeight * sy).toInt()
            layoutParams.topMargin = 0
            layoutParams.leftMargin = abs((viewWidth - (videoWidth * sy).toInt()) / 2)
        } else {
            layoutParams.width = (videoWidth * sx).toInt()
            layoutParams.height = (videoHeight * sx).toInt()
            layoutParams.topMargin = abs((viewHeight - (videoHeight * sx).toInt()) / 2)
            layoutParams.leftMargin = 0
        }
        textureView.layoutParams = layoutParams
    }

    fun pauseStateChanged() {
        if(mMediaPlayer!!.isPlaying) {
            mMediaPlayer?.pause()
            (mVideoPlayerView as VideoPlayerActivity).showPauseView(true)
        }else {
            mMediaPlayer?.start()
            (mVideoPlayerView as VideoPlayerActivity).showPauseView(false)

        }
    }

    fun screenOrientationChanged (isVertical: Boolean, videoTextureView: TextureView,
                                  widthPixels: Int, heightPixels: Int) {
        adjustTextureViewSize(
            videoTextureView,
            widthPixels, heightPixels,
            mMediaPlayer!!.videoWidth, mMediaPlayer!!.videoHeight)

        (mVideoPlayerView as VideoPlayerActivity).showOrientationView(isVertical)
    }

    fun getAllVideoTime(): Int? {
        return mMediaPlayer?.duration
    }

    fun getCurrentVideoTime(): Int? {
        return mMediaPlayer?.currentPosition
    }

    fun setSeekbarChangingFlag(isSeekbarChanging: Boolean) {
        mIsSeekbarChanging = isSeekbarChanging
    }

    fun videoSeekTo(seekPosition: Int) {
        mMediaPlayer?.seekTo(seekPosition)
    }

    fun updateFunctionValue(gestureType: Int, startValue: Float, currentValue: Float) {
        when (gestureType) {
            VideoPlayerView.ADJUST_VOLUME -> {
                val height: Float = (mVideoPlayerView as VideoPlayerActivity).getVideoViewHeight().toFloat()
                if (mMaxVolumeValue != null && mMaxVolumeValue!! > 0 && mVolumeValue != null && mVolumeValue!! >= 0) {
                    val adjustValue = -(currentValue - startValue) * 2f / height * mMaxVolumeValue!!
                    val currentVolumeValue = 0f.coerceAtLeast(mVolumeValue!! + adjustValue).coerceAtMost(mMaxVolumeValue!!)
                    (mVideoPlayerView as VideoPlayerActivity).updateFunctionSeekBar(gestureType, currentVolumeValue)
                }
            }
            VideoPlayerView.ADJUST_LIGHT -> {
                val height: Float = (mVideoPlayerView as VideoPlayerActivity).getVideoViewHeight().toFloat()
                val adjustValue = -(currentValue - startValue) * 2f / height
                if (mLightValue != null && mLightValue!! >= 0) {
                    val currentLightValue = 0f.coerceAtLeast(mLightValue!! + adjustValue).coerceAtMost(1f)
                    (mVideoPlayerView as VideoPlayerActivity).updateFunctionSeekBar(gestureType, currentLightValue)
                }
            }
            VideoPlayerView.ADJUST_VIDEO_TIME -> {
                val adjustValue = (currentValue - startValue) * 60 * 5
                val allVideoTime = getAllVideoTime()
                val currentTime = getCurrentVideoTime()
                if (allVideoTime != null && currentTime != null) {
                    val adjustTime = 0f.coerceAtLeast(currentTime + adjustValue).coerceAtMost(allVideoTime.toFloat())
                    mSeekTime = adjustTime.toInt()
                    (mVideoPlayerView as VideoPlayerActivity).updateFunctionSeekBar(gestureType, adjustTime)
                }
            }
        }
    }

    fun seekToGestureTime() {
        mSeekTime?.let {
            val allVideoTime = getAllVideoTime()
            if (allVideoTime != null && it >= 0 && it <= allVideoTime) {
                videoSeekTo(it)
            }
        }
    }

    fun changePlayerSpeed(speed: Float): Boolean {
        val playbackParams = mMediaPlayer?.playbackParams?.setSpeed(speed)
        if (playbackParams != null) {
            mMediaPlayer?.playbackParams = playbackParams
            return true
        } else {
            return false
        }
    }

    fun resetPlayerSpeed() {
        val playbackParams = mMediaPlayer?.playbackParams
        if (playbackParams != null && playbackParams.speed != 1.0f) {
            playbackParams.setSpeed(1.0f)
            mMediaPlayer?.playbackParams = playbackParams
        }
    }
}