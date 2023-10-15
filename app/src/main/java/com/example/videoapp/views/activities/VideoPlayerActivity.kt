package com.example.videoapp.views.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.videoapp.ConfigParams
import com.example.videoapp.R
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.interfaces.VideoView
import com.example.videoapp.presenters.VideoPlayerPresenter
import com.example.videoapp.views.customviews.OnDoubleClickListener
import com.example.videoapp.utils.VideoUtils

class VideoPlayerActivity : AppCompatActivity(), VideoView {
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

    private var mVideoPlayerPresenter: VideoPresenter = VideoPlayerPresenter()

    init {
        mVideoPlayerPresenter.setView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        (mVideoPlayerPresenter as VideoPlayerPresenter).initMediaPlayer()
        mVideoTextureView.surfaceTextureListener = object: SurfaceTextureListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                (mVideoPlayerPresenter as VideoPlayerPresenter).startMediaPlayer(
                    intent.getStringExtra("video_url")!!,
                    mVideoTextureView, mVideoSeekBar,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels-ConfigParams.viewHeightOffset
                )

                mVideoTextureView.setOnTouchListener(
                    OnDoubleClickListener(object : OnDoubleClickListener.DoubleClickCallback{
                        override fun onDoubleClick() {
                            (mVideoPlayerPresenter as VideoPlayerPresenter).pauseStateChanged()
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
                (mVideoPlayerPresenter as VideoPlayerPresenter).destroyMediaPlayer()
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

            }

        }
    }

    override fun onStop() {
        super.onStop()
        (mVideoPlayerPresenter as VideoPlayerPresenter).destroyMediaPlayer()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //变成横屏了
            (mVideoPlayerPresenter as VideoPlayerPresenter).screenOrientationChanged(
                false, mVideoTextureView,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels-ConfigParams.viewHeightOffset
            )
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //变成竖屏了
            (mVideoPlayerPresenter as VideoPlayerPresenter).screenOrientationChanged(
                true, mVideoTextureView,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels-ConfigParams.viewHeightOffset
            )
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

    fun videoTimeChanged(startTime: Int?, endTime: Int?) {
        if(startTime == null || endTime == null) {
            return
        }
        mVideoStartTime.text = VideoUtils.calculateTime(startTime / 1000) //开始时间
        mVideoEndTime.text = VideoUtils.calculateTime(endTime / 1000) //总时长
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

    override fun setPresenter(presenter: VideoPresenter) {
        mVideoPlayerPresenter = presenter
    }
}