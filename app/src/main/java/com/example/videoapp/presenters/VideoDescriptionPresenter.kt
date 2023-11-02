package com.example.videoapp.presenters

import android.graphics.Bitmap
import com.example.videoapp.MainActivity
import com.example.videoapp.R
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoModel
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.interfaces.VideoView
import com.example.videoapp.models.VideoDescriptionModel
import com.example.videoapp.utils.VideoUtils
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

class VideoDescriptionPresenter: VideoPresenter {
    private var mDescriptionModel: VideoModel? = null
    private var mDescriptionView: VideoView? = null

    init {
        mDescriptionModel = VideoDescriptionModel()
        (mDescriptionModel as VideoDescriptionModel).setPresenter(this)
    }

    private val mBlankVideoImage: Bitmap by lazy {
        VideoUtils.vectorDrawableToBitmap(mDescriptionView as MainActivity, R.drawable.blank_video_image)
    }
    private var mJsonDictStream: InputStream? = null
    override fun setModel(model: VideoModel) {
        mDescriptionModel = model
    }

    override fun setView(view: VideoView) {
        mDescriptionView = view
    }

    fun initServerData() {
        CoroutineScope(Dispatchers.IO).launch{
            mJsonDictStream = (mDescriptionView as MainActivity).resources.openRawResource(R.raw.complete_video_data)
            // TODO 这里需要判断键值，防止FC
            val jsonObject = VideoUtils.parseJSONtoDict(mJsonDictStream!!).getJSONObject("奈汐酱")
            val videoEntities = (mDescriptionModel as VideoDescriptionModel).getServerData(jsonObject,
                mBlankVideoImage, false, "奈汐酱")
            (mDescriptionView as MainActivity).showVideoInfoRecyclerView(videoEntities)
        }
    }

    fun updateServerData(isDown: Boolean, refreshLayout: RefreshLayout, refreshOperation: (RefreshLayout) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            mJsonDictStream = (mDescriptionView as MainActivity).resources.openRawResource(R.raw.complete_video_data)
            val jsonObject = VideoUtils.parseJSONtoDict(mJsonDictStream!!).getJSONObject("奈汐酱")
            // TODO 这里需要判断键值，防止FC
            val videoEntities = (mDescriptionModel as VideoDescriptionModel).getServerData(jsonObject,
                mBlankVideoImage, isDown, "奈汐酱")
            (mDescriptionView as MainActivity).updateVideoInfoRecyclerView(videoEntities, isDown,
                refreshLayout, refreshOperation)
        }
    }

    fun getDetailData(videoEntity: VideoEntity): ArrayList<VideoEntity> {
        val detailData = ArrayList<VideoEntity>()
        var detailEntity: VideoEntity

        //TODO 需不需要为判空？？？？？
        for(videoMessage in videoEntity.mBitmapArray!!){
            detailEntity = VideoEntity(-1, videoMessage.first, "XXX", videoMessage.second)
            detailData.add(detailEntity)
        }

        return detailData
    }
}