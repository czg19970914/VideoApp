package com.example.videoapp.models

import android.graphics.Bitmap
import com.example.videoapp.ConfigParams
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoModel
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.models.Cache.VideoEntityCache
import com.example.videoapp.utils.VideoUtils
import com.ywl5320.wlmedia.WlMediaUtil
import org.json.JSONObject

class VideoDescriptionModel: VideoModel {
    private var mDescriptionPresenter: VideoPresenter? = null

    private var mWlMediaUtil: WlMediaUtil? = null
    private var mMinId = 1
    private var mMaxId = 23
    override fun setPresenter(presenter: VideoPresenter) {
        mDescriptionPresenter = presenter
    }

    fun getServerData(jsonObject: JSONObject, blankViewImage: Bitmap, isDown: Boolean, detailName: String): ArrayList<VideoEntity> {
        val videEntities = ArrayList<VideoEntity>()

        var selectId = mMinId - 1
        if(isDown)
            selectId = mMaxId + 1

        if(selectId < 0 || selectId >= jsonObject.length()) {
            return videEntities
        }

        var videoEntity: VideoEntity

        var cachedKey: String
        var cachedEntity: VideoEntity?

        mMinId = Math.max(0, selectId - ConfigParams.getDescriptionNum / 2)
        mMaxId = Math.min(jsonObject.length() - 1, selectId + ConfigParams.getDescriptionNum / 2)
        for(id in mMinId..mMaxId) {
            cachedKey = detailName + "_" + id.toString()
            if(VideoEntityCache.getInstance()?.containsKey(cachedKey) == true){
                cachedEntity = VideoEntityCache.getInstance()?.get(cachedKey)
                if (cachedEntity != null) {
                    videEntities.add(cachedEntity)
                }
            }else{
                videoEntity = analysisVideoJSONObject(jsonObject, id, blankViewImage)
                videEntities.add(videoEntity)
                VideoEntityCache.getInstance()?.put(cachedKey, videoEntity)
            }
        }

        return videEntities
    }

    private fun analysisVideoJSONObject(jsonObject: JSONObject, id: Int, blankViewImage: Bitmap): VideoEntity {
        mWlMediaUtil = WlMediaUtil()

        val videoTitle = "测试文本!!!!"
        val array = jsonObject.getJSONArray(id.toString())
        //TODO 这里是否要对arry长度判断，防止FC
        var completeUrl = ConfigParams.baseUrl + array[0].toString()
        var videoImage = VideoUtils.getVideoImage(completeUrl, mWlMediaUtil!!, blankViewImage)
        val videoEntity = VideoEntity(id, completeUrl, videoTitle, videoImage)
        val videoBitmaps = ArrayList<Pair<String, Bitmap>>()

        videoBitmaps.add(Pair(completeUrl, videoImage))
        for(index in 1 until array.length()) {
            mWlMediaUtil = WlMediaUtil()
            completeUrl = ConfigParams.baseUrl + array[index].toString()
            videoImage = VideoUtils.getVideoImage(completeUrl, mWlMediaUtil!!, blankViewImage)
            videoBitmaps.add(Pair(completeUrl, videoImage))
        }
        videoEntity.mBitmapArray = videoBitmaps

        return videoEntity
    }

    fun resetIndex(){
        mMinId = 1
        mMaxId = 23
    }
}