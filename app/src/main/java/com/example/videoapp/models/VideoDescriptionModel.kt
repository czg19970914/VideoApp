package com.example.videoapp.models

import android.graphics.Bitmap
import com.example.videoapp.ConfigParams
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoModel
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.models.Cache.DescriptionCache
import com.example.videoapp.utils.VideoUtils
import com.ywl5320.wlmedia.WlMediaUtil
import java.io.InputStream

class VideoDescriptionModel: VideoModel {
    private var mDescriptionPresenter: VideoPresenter? = null

    private var mWlMediaUtil: WlMediaUtil? = null
    private var mMinId = 1
    private var mMaxId = 23
    override fun setPresenter(presenter: VideoPresenter) {
        mDescriptionPresenter = presenter
    }

    fun getServerDataDict(jsonStream: InputStream, blankViewImage: Bitmap, isDown: Boolean): ArrayList<VideoEntity> {
        val videEntities = ArrayList<VideoEntity>()
        val jsonObject = VideoUtils.parseJSONtoDict(jsonStream)

        var selectId = mMinId - 1
        if(isDown)
            selectId = mMaxId + 1

        if(selectId < 0 || selectId >= jsonObject.length()) {
            return videEntities
        }

        var completeUrl = ""
        var videoTitle = ""
        var videoImage: Bitmap
        var videoEntity: VideoEntity

        mMinId = Math.max(0, selectId - ConfigParams.getDescriptionNum / 2)
        mMaxId = Math.min(jsonObject.length() - 1, selectId + ConfigParams.getDescriptionNum / 2)
        for(id in mMinId..mMaxId) {
            mWlMediaUtil = WlMediaUtil()
            completeUrl = ConfigParams.baseUrl + jsonObject.getJSONObject(id.toString()).get("file_name").toString()
//            videoTitle = jsonObject.getJSONObject(id.toString()).get("video_title").toString()
            videoTitle = "测试文本!!!!"
            if(DescriptionCache.getInstance()?.containsKey(id) == true) {
                videoImage = DescriptionCache.getInstance()?.get(id)!!
            }else {
//                videoImage = VideoUtils.getVideoImage(completeUrl, mWlMediaUtil!!, blankViewImage)
                videoImage = blankViewImage
                DescriptionCache.getInstance()?.put(id, videoImage)
            }
            videoEntity = VideoEntity(id, completeUrl, videoTitle, videoImage)
            videEntities.add(videoEntity)
        }
        return videEntities
    }
}