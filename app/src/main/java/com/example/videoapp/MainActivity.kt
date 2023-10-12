package com.example.videoapp

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.customviews.VideoAsyncTask
import com.example.videoapp.recyclerviews.VideoRecyclerViewAdapter
import com.example.videoapp.utils.VideoUtils
import com.ywl5320.wlmedia.WlMediaUtil
import com.example.videoapp.entities.VideoEntity
import kotlinx.coroutines.*
import java.io.InputStream
import java.util.concurrent.CopyOnWriteArrayList


class MainActivity : AppCompatActivity() {
    private val mVideoRecyclerView: RecyclerView by lazy {
        findViewById(R.id.video_recycler_view)
    }
    private val mBlankVideoImage: Bitmap by lazy {
        VideoUtils.vectorDrawableToBitmap(this, R.drawable.blank_video_image)
    }

    private var mWaitingDialogBuilder: AlertDialog.Builder? = null
    private var mWaitingDialog: AlertDialog? = null

    private var mWlMediaUtil: WlMediaUtil? = null

    private var mVideoEntities: List<VideoEntity>? = null
    private var mVideoListAdapter: VideoRecyclerViewAdapter? = null

    private var mAddResourceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.light_gray)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.light_gray)
        initWaitingDialog()

        // 协程多线程来加载资源链表
        mWaitingDialog = mWaitingDialogBuilder?.show()
        mAddResourceJob = CoroutineScope(Dispatchers.IO).launch {
            // 多协程来请求网络，但是有问题后续优化
//            val videEntities = CopyOnWriteArrayList<VideoEntity>()
//            val getNetWorkInfoJob = launch {
//                val jsonStream: InputStream = resources.openRawResource(R.raw.video_data)
//                val jsonArray = VideoUtils.parseJSON(jsonStream)
//
//                var completeUrl = ""
//                var videoTitle = ""
//                var videoImage: Bitmap
//                var videoEntity: VideoEntity
//
//                for(i in 0 until jsonArray.size()) {
//                    launch {
//                        mWlMediaUtil = WlMediaUtil()
//                        completeUrl = ConfigParams.baseUrl + jsonArray[i].asJsonObject.get("file_name").asString
//                        videoTitle = jsonArray[i].asJsonObject.get("video_title").asString
//                        videoImage = VideoUtils.getVideoImage(completeUrl, mWlMediaUtil!!, mBlankVideoImage)
//                        videoEntity = VideoEntity(completeUrl, videoTitle, videoImage)
//                        videEntities.add(videoEntity)
//                    }
//                }
//            }
//            getNetWorkInfoJob.join()
//            mVideoEntities = videEntities
            mVideoEntities = initVideoEntities()
            showVideoInfoRecyclerView()
        }
    }

    private fun initVideoEntities(): List<VideoEntity> {
        val videEntities = ArrayList<VideoEntity>()
//        val videEntities = CopyOnWriteArrayList<VideoEntity>()

        val jsonStream: InputStream = resources.openRawResource(R.raw.video_data)
        val jsonArray = VideoUtils.parseJSON(jsonStream)

        var completeUrl = ""
        var videoTitle = ""
        var videoImage: Bitmap
        var videoEntity: VideoEntity

        for(i in 0 until jsonArray.size()) {
            mWlMediaUtil = WlMediaUtil()
            completeUrl = ConfigParams.baseUrl + jsonArray[i].asJsonObject.get("file_name").asString
            videoTitle = jsonArray[i].asJsonObject.get("video_title").asString
            videoImage = VideoUtils.getVideoImage(completeUrl, mWlMediaUtil!!, mBlankVideoImage)
            videoEntity = VideoEntity(completeUrl, videoTitle, videoImage)
            videEntities.add(videoEntity)
        }

        return videEntities
    }

    private fun initRecyclerView(videoEntities: List<VideoEntity>) {
        mVideoListAdapter = VideoRecyclerViewAdapter(this, videoEntities)
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 2)
        mVideoRecyclerView.layoutManager = layoutManager
        mVideoRecyclerView.adapter = mVideoListAdapter
    }

    private fun initWaitingDialog(){
        mWaitingDialogBuilder = AlertDialog.Builder(this)
        mWaitingDialogBuilder!!.setCancelable(false)
        mWaitingDialogBuilder!!.setView(R.layout.dialog_waiting)
    }

    private suspend fun showVideoInfoRecyclerView()= withContext(Dispatchers.Main) {
        mVideoEntities?.let {
            mWaitingDialog?.dismiss()
            initRecyclerView(it)
        }
    }
}