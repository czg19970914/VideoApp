package com.example.videoapp.models

import android.graphics.Bitmap
import android.util.Log
import com.example.videoapp.ConfigParams
import com.example.videoapp.entities.NameEntity
import com.example.videoapp.entities.SubVideoDescriptionEntity
import com.example.videoapp.entities.VideoDescriptionEntity
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoModel
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.models.Cache.VideoEntityCache
import com.example.videoapp.network.NetworkService
import com.example.videoapp.utils.VideoUtils
import com.ywl5320.wlmedia.WlMediaUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject

class VideoDescriptionModel: VideoModel {
    companion object{
        const val TAG = "VideoDescriptionModel"
    }

    private var mDescriptionPresenter: VideoPresenter? = null

    private var mWlMediaUtil: WlMediaUtil? = null
    private var mMinId = 1
    private var mMaxId = 23

    private var networkService: NetworkService? = null

    init {
        networkService = NetworkService.createService()
    }
    override fun setPresenter(presenter: VideoPresenter) {
        mDescriptionPresenter = presenter
    }

    fun getServerData(jsonObject: JSONObject, blankViewImage: Bitmap,
                      isDown: Boolean, detailName: String): ArrayList<VideoEntity> {
        val videEntities = ArrayList<VideoEntity>()

        var selectId = mMinId - 1
        if(isDown)
            selectId = mMaxId + 1

        if(selectId < 0 || selectId >= jsonObject.length()) {
            return videEntities
        }

        var videoEntity: VideoEntity?

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

                if(videoEntity != null) {
                    videEntities.add(videoEntity)
                    VideoEntityCache.getInstance()?.put(cachedKey, videoEntity)
                }
            }
        }

        return videEntities
    }

    /**
     * TODO 这部分代码的健壮性需要大大加强啊，问题不小
     * **/
    suspend fun getServerDataByInternet(selectName: String, isDown: Boolean, blankViewImage: Bitmap):
            ArrayList<VideoEntity> {
        val videEntities = ArrayList<VideoEntity>()

        var selectId = mMinId - 1
        if(isDown)
            selectId = mMaxId + 1

        mMinId = selectId - ConfigParams.getDescriptionNum / 2
        mMaxId = selectId + ConfigParams.getDescriptionNum / 2

        val videoDescriptionMap = networkService?.getVideoDescriptionMap(selectName, mMinId, mMaxId)
            ?: return videEntities
        // 如果id超过边界重置id
        mMinId = 0.coerceAtLeast(mMinId)
        mMaxId = videoDescriptionMap.size.coerceAtMost(mMaxId)

        for((key : String, value : VideoDescriptionEntity) in videoDescriptionMap) {
            val videoTitle = key
            var completeUrl = ""
            var videoImage = blankViewImage
            val videoBitmaps = ArrayList<Pair<String, Bitmap>>()
            for(subVideo : SubVideoDescriptionEntity? in value.subImages!!) {
                if(subVideo == null) {
                    continue
                }
                completeUrl = ConfigParams.baseUrl + subVideo.subVideoPath!!
                videoImage = VideoUtils.base64ToBitmap(subVideo.subVideoImage, blankViewImage)
                videoBitmaps.add(
                    Pair(
                        completeUrl, videoImage
                    )
                )
            }
            val videoEntity = VideoEntity(0, completeUrl, videoTitle, videoImage)
            videoEntity.mBitmapArray = videoBitmaps
            videEntities.add(videoEntity)
        }
        return videEntities
    }

    private fun analysisVideoJSONObject(jsonObject: JSONObject, id: Int,
                                        blankViewImage: Bitmap): VideoEntity? {
        try {
            mWlMediaUtil = WlMediaUtil()

            val videoTitle = "测试文本!!!!"
            val array = jsonObject.getJSONArray(id.toString())
            var completeUrl = ConfigParams.baseUrl + array[0].toString()
            var videoImage = VideoUtils.getVideoImage(completeUrl, mWlMediaUtil!!, blankViewImage)
            val videoBitmaps = ArrayList<Pair<String, Bitmap>>()
            videoBitmaps.add(Pair(completeUrl, videoImage))
            for(index in 1 until array.length()) {
                mWlMediaUtil = WlMediaUtil()
                completeUrl = ConfigParams.baseUrl + array[index].toString()
                videoImage = VideoUtils.getVideoImage(completeUrl, mWlMediaUtil!!, blankViewImage)
                videoBitmaps.add(Pair(completeUrl, videoImage))
            }
            val videoEntity = VideoEntity(id, completeUrl, videoTitle, videoImage)
            videoEntity.mBitmapArray = videoBitmaps

            return videoEntity
        }catch (e: NoSuchElementException) {
            e.message?.let { Log.e(TAG, it) }
        }catch (e: IndexOutOfBoundsException) {
            e.message?.let { Log.e(TAG, it) }
        }catch (e: NullPointerException) {
            e.message?.let { Log.e(TAG, it) }
        }

        return null
    }

    fun getNameList(jsonObject: JSONObject): ArrayList<NameEntity>{
        val nameList = ArrayList<NameEntity>()
        for(key in jsonObject.keys()) {
            nameList.add(NameEntity(key, false))
        }
        return nameList
    }

    suspend fun getNameListByIntent(): ArrayList<NameEntity>{
        val videoDescriptionBarNames = networkService?.getVideoDescriptionBarNames()
        val nameList = ArrayList<NameEntity>()
        if(videoDescriptionBarNames != null) {
            for (name in videoDescriptionBarNames) {
                nameList.add(NameEntity(name, false))

            }
        }
        return nameList
    }

    fun resetIndex(){
        mMinId = 1
        mMaxId = 23
    }
}