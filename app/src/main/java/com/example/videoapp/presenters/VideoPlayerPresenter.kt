package com.example.videoapp.presenters

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
import java.util.*
import kotlin.math.abs

class VideoPlayerPresenter: VideoPresenter {
    private var mVideoPlayerView: VideoView? = null

    private var mMediaPlayer: MediaPlayer? = null
    private var mSeekBarTimer: Timer? = null
    private var mIsSeekbarChanging = false // 互斥变量，防止进度条和定时器冲突
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
}