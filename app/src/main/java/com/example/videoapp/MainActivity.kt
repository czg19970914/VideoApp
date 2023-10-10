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
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private val mVideoRecyclerView: RecyclerView by lazy {
        findViewById(R.id.video_recycler_view)
    }

    private var mWaitingDialogBuilder: AlertDialog.Builder? = null
    private var mWaitingDialog: AlertDialog? = null

    private var mWlMediaUtil: WlMediaUtil? = null

    private var mVideoEntities: List<VideoEntity>? = null
    private var mVideoListAdapter: VideoRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.light_gray)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.light_gray)
        initWaitingDialog()

        // 这里AsyncTask已经被废弃，用kotlin协程代替，之后再学
        object : VideoAsyncTask(this){
            override fun doInBackground(vararg p0: Unit?) {
                mVideoEntities = initVideoEntities()
            }

            override fun onPreExecute() {
                super.onPreExecute()
                mWaitingDialog = mWaitingDialogBuilder?.show()
            }

            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                mVideoEntities?.let {
                    mWaitingDialog?.dismiss()
                    initRecyclerView(it)
                }
            }
        }.execute()


    }

    private fun initVideoEntities(): List<VideoEntity> {
        val videEntities = ArrayList<VideoEntity>()
        val blankVideoImage = VideoUtils.vectorDrawableToBitmap(this, R.drawable.blank_video_image)

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
            videoImage = VideoUtils.getVideoImage(completeUrl, mWlMediaUtil!!, blankVideoImage)
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
}