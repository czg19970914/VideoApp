package com.example.videoapp.presenters

import android.graphics.Bitmap
import android.util.Log
import com.example.videoapp.MainActivity
import com.example.videoapp.R
import com.example.videoapp.entities.NameEntity
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoModel
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.interfaces.VideoView
import com.example.videoapp.models.VideoDescriptionModel
import com.example.videoapp.network.NetworkService
import com.example.videoapp.utils.VideoUtils
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

class VideoDescriptionPresenter: VideoPresenter {
    companion object{
        const val TAG = "VideoDescriptionPresenter"
    }

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
    private var mJsonDictStream: InputStream? = null
    override fun setModel(model: VideoModel) {
        mDescriptionModel = model
    }

    override fun setView(view: VideoView) {
        mDescriptionView = view
    }

    fun getNameList() {
        CoroutineScope(Dispatchers.IO).launch{
            var nameList = ArrayList<NameEntity>()
            try {
//                mJsonDictStream = (mDescriptionView as MainActivity).resources.openRawResource(
//                    R.raw.complete_video_data
//                )
//                val jsonObject = VideoUtils.parseJSONtoDict(mJsonDictStream!!)
//                nameList = (mDescriptionModel as VideoDescriptionModel).getNameList(jsonObject)
                nameList = (mDescriptionModel as VideoDescriptionModel).getNameListByIntent()
            }catch (e: IOException){
                e.message?.let { Log.e(TAG, it) }
            } catch (e: NullPointerException){
                e.message?.let { Log.e(TAG, it) }
            } finally {
                (mDescriptionView as MainActivity).showSelectBar(nameList)
            }
        }
    }

    fun getServerData(selectName: String, isInit: Boolean) {
        CoroutineScope(Dispatchers.IO).launch{
            var videoEntities = ArrayList<VideoEntity>()
            try {
//                mJsonDictStream = (mDescriptionView as MainActivity).resources.openRawResource(
//                    R.raw.complete_video_data
//                )
//                val jsonObject = VideoUtils.parseJSONtoDict(mJsonDictStream!!).getJSONObject(selectName)
//                // 注意这里需要重置model中算法的id!!!!!
//                (mDescriptionModel as VideoDescriptionModel).resetIndex()
//                videoEntities = (mDescriptionModel as VideoDescriptionModel).getServerData(
//                    jsonObject, mBlankVideoImage, false, selectName)

                // 注意这里需要重置model中算法的id!!!!!
                (mDescriptionModel as VideoDescriptionModel).resetIndex()
                videoEntities = (mDescriptionModel as VideoDescriptionModel).getServerDataByInternet(
                    selectName, false, mBlankVideoImage
                )
            }catch (e: IOException){
                e.message?.let { Log.e(TAG, it) }
            }catch (e: NullPointerException){
                e.message?.let { Log.e(TAG, it) }
            } catch (e: NoSuchElementException) {
                e.message?.let { Log.e(TAG, it) }
            } finally {
                if(isInit)
                    (mDescriptionView as MainActivity).showVideoInfoRecyclerView(videoEntities)
                else
                    (mDescriptionView as MainActivity).switchNameRecyclerView(videoEntities)
            }
        }
    }

    fun updateServerData(selectName: String, isDown: Boolean, refreshLayout: RefreshLayout,
                         refreshOperation: (RefreshLayout) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var videoEntities = ArrayList<VideoEntity>()
            try {
//                mJsonDictStream = (mDescriptionView as MainActivity).resources.openRawResource(
//                    R.raw.complete_video_data
//                )
//                val jsonObject = VideoUtils.parseJSONtoDict(mJsonDictStream!!).getJSONObject(selectName)
//                videoEntities = (mDescriptionModel as VideoDescriptionModel).getServerData(
//                    jsonObject, mBlankVideoImage, isDown, selectName)

                videoEntities = (mDescriptionModel as VideoDescriptionModel).getServerDataByInternet(
                    selectName, isDown, mBlankVideoImage
                )
            }catch (e: IOException){
                e.message?.let { Log.e(TAG, it) }
            }catch (e: NullPointerException){
                e.message?.let { Log.e(TAG, it) }
            } catch (e: NoSuchElementException) {
                e.message?.let { Log.e(TAG, it) }
            } finally {
                (mDescriptionView as MainActivity).updateVideoInfoRecyclerView(videoEntities, isDown,
                    refreshLayout, refreshOperation)
            }
        }
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