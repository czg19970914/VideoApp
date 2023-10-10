package com.example.videoapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.videoapp.ConfigParams
import com.example.videoapp.R
import com.example.videoapp.customviews.OnDoubleClickListener
import com.example.videoapp.utils.VideoUtils
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

class VideoPlayerActivity : AppCompatActivity() {
    private val mVideoTextureView: TextureView by lazy {
        findViewById(R.id.video_texture_view)
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

    private var mMediaPlayer: MediaPlayer? = null
    private var mSeekBarTimer: Timer? = null
    private var mIsSeekbarChanging = false // 互斥变量，防止进度条和定时器冲突

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        mMediaPlayer = MediaPlayer()
        mSeekBarTimer = Timer()
        mVideoTextureView.surfaceTextureListener = object: SurfaceTextureListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                startMediaPlayer(intent.getStringExtra("video_url")!!)

                mVideoTextureView.setOnTouchListener(
                    OnDoubleClickListener(object : OnDoubleClickListener.DoubleClickCallback{
                        override fun onDoubleClick() {
                            if(mMediaPlayer!!.isPlaying) {
                                mMediaPlayer?.pause()
                                videoPauseOrPlay(true)
                            }else {
                                mMediaPlayer?.start()
                                videoPauseOrPlay(false)

                            }
                        }

                    })
                )

                mHorizontalScreenButton.setOnClickListener {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                mVerticalScreenButton.setOnClickListener {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                mSeekBarTimer?.cancel()
                mMediaPlayer?.release()
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

            }

        }
    }

    override fun onStop() {
        super.onStop()
        mSeekBarTimer?.cancel()
        mSeekBarTimer = null

        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //变成横屏了
            screenOrientationChanged(false)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //变成竖屏了
            screenOrientationChanged(true)
        }
    }

    companion object {
        @JvmStatic
        fun startVideoPlayerActivity(context: Context, url: String) {
            val intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtra("video_url", url)

            context.startActivity(intent)
        }
    }

    private fun startMediaPlayer(url: String) {

        mMediaPlayer?.setSurface(Surface(mVideoTextureView.surfaceTexture))

        // 解决视频拉伸问题
        mMediaPlayer?.setOnVideoSizeChangedListener { p0, p1, p2 ->
            adjustTextureViewSize(mVideoTextureView,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels-ConfigParams.viewHeightOffset,
                p1, p2)
        }

        val uri = Uri.parse(url)
        mMediaPlayer?.setDataSource(this, uri)
        mMediaPlayer?.prepareAsync()

        mMediaPlayer?.setOnPreparedListener {
            it.start()
            startVideoSeekBar()
        }
        mMediaPlayer?.setOnCompletionListener {
            it.seekTo(0)
            videoPauseOrPlay(true)
        }

    }

    private fun adjustTextureViewSize(textureView: TextureView,
                                      viewWidth: Int, viewHeight: Int,
                                      videoWidth: Int, videoHeight: Int) {
        val layoutParams: ConstraintLayout.LayoutParams = textureView.layoutParams as ConstraintLayout.LayoutParams

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

    private fun startVideoSeekBar() {
        var startTime = mMediaPlayer?.currentPosition //获取当前播放的位置
        val endTime = mMediaPlayer?.duration //获取音乐总时长
        videoTimeChanged(startTime, endTime)

        mVideoSeekBar.max = endTime!!
        mVideoSeekBar.setOnSeekBarChangeListener(
            object :SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    startTime = mMediaPlayer?.currentPosition //获取当前播放的位置
                    videoTimeChanged(startTime, endTime)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    mIsSeekbarChanging = true
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    mIsSeekbarChanging = false

                    if (p0 != null) {
                        mMediaPlayer?.seekTo(p0.progress)
                        startTime = mMediaPlayer?.currentPosition //获取当前播放的位置
                        videoTimeChanged(startTime, endTime)
                    }
                }

            }
        )
        mSeekBarTimer?.schedule(
            object : TimerTask() {
                override fun run() {
                    if(!mIsSeekbarChanging) {
                        mVideoSeekBar.progress = mMediaPlayer!!.currentPosition
                    }
                }
            }, 0, 50
        )
    }

    fun videoTimeChanged(startTime: Int?, endTime: Int?) {
        if(startTime == null || endTime == null) {
            return
        }
        mVideoStartTime.text = VideoUtils.calculateTime(startTime / 1000) //开始时间
        mVideoEndTime.text = VideoUtils.calculateTime(endTime / 1000) //总时长
    }

    private fun screenOrientationChanged(isVertical: Boolean) {
        adjustTextureViewSize(mVideoTextureView,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels-ConfigParams.viewHeightOffset,
            mMediaPlayer!!.videoWidth, mMediaPlayer!!.videoHeight)

        if(isVertical) {
            mVerticalScreenButton.visibility = View.GONE
            mHorizontalScreenButton.visibility = View.VISIBLE
        }else {
            mVerticalScreenButton.visibility = View.VISIBLE
            mHorizontalScreenButton.visibility = View.GONE
        }
    }

    private fun videoPauseOrPlay(isPaused: Boolean) {
        if(isPaused) {
            mVideoPausedImage.visibility = View.VISIBLE
            mVideoPlayedImage.visibility = View.GONE
        }else {
            mVideoPausedImage.visibility = View.GONE
            mVideoPlayedImage.visibility = View.VISIBLE
        }
    }
}