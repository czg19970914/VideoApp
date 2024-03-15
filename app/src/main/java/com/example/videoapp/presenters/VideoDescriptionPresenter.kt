package com.example.videoapp.presenters

import android.graphics.Bitmap
import com.example.videoapp.MainActivity
import com.example.videoapp.R
import com.example.videoapp.entities.NameEntity
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoModel
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.interfaces.VideoView
import com.example.videoapp.models.VideoDescriptionModel
import com.example.videoapp.utils.VideoUtils
import com.scwang.smart.refresh.layout.api.RefreshLayout

class VideoDescriptionPresenter: VideoPresenter {
//    companion object{
//        const val TAG = "VideoDescriptionPresenter"
//    }

    private var mDescriptionModel: VideoModel? = null
    private var mDescriptionView: VideoView? = null

    init {
        mDescriptionModel = VideoDescriptionModel()
        (mDescriptionModel as VideoDescriptionModel).setPresenter(this)
    }

    private val mBlankVideoImage: Bitmap by lazy {
        VideoUtils.vectorDrawableToBitmap(
            mDescriptionView as MainActivity, R.drawable.blank_video_image)
    }
    override fun setModel(model: VideoModel) {
        mDescriptionModel = model
    }

    override fun setView(view: VideoView) {
        mDescriptionView = view
    }

    fun getNameList() {
        (mDescriptionModel as VideoDescriptionModel).initVideoDescriptionData(
            (mDescriptionView as MainActivity).baseContext
        )
    }

    suspend fun showSelectBar(nameList: ArrayList<NameEntity>) {
        (mDescriptionView as MainActivity).showSelectBar(nameList)
    }

    fun getServerData(selectName: String, isInit: Boolean) {
        // 注意这里需要重置model中算法的id!!!!!
        (mDescriptionModel as VideoDescriptionModel).resetIndex()
        (mDescriptionModel as VideoDescriptionModel).getSelectVideoDescription(
            (mDescriptionView as MainActivity).baseContext, selectName,
            false, mBlankVideoImage, isInit
        )
    }

    suspend fun initVideoInfoRecyclerView(isInit: Boolean, videoEntities: ArrayList<VideoEntity>) {
        if(isInit)
                (mDescriptionView as MainActivity).showVideoInfoRecyclerView(videoEntities)
            else
                (mDescriptionView as MainActivity).switchNameRecyclerView(videoEntities)
    }

    fun updateServerData(selectName: String, isDown: Boolean, refreshLayout: RefreshLayout,
                         refreshOperation: (RefreshLayout) -> Unit) {

        (mDescriptionModel as VideoDescriptionModel).updateSelectVideoDescription(
            (mDescriptionView as MainActivity).baseContext, selectName, isDown,
            mBlankVideoImage, refreshLayout, refreshOperation
        )
    }

    suspend fun updateVideoInfoRecyclerView(videoEntities: ArrayList<VideoEntity>,
                                            isDown: Boolean, refreshLayout: RefreshLayout,
                                            refreshOperation: (RefreshLayout) -> Unit) {
        (mDescriptionView as MainActivity).updateVideoInfoRecyclerView(videoEntities, isDown,
            refreshLayout, refreshOperation)
    }

    fun getDetailData(videoEntity: VideoEntity): ArrayList<VideoEntity> {
        val detailData = ArrayList<VideoEntity>()
        var detailEntity: VideoEntity

        if(videoEntity.mBitmapArray == null) {
            return detailData
        }
        for(videoMessage in videoEntity.mBitmapArray!!){
            detailEntity = VideoEntity(
                -1, videoMessage.first, "XXX", videoMessage.second)
            detailData.add(detailEntity)
        }

        return detailData
    }
}