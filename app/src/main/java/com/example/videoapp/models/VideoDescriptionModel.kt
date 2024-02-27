package com.example.videoapp.models

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.videoapp.ConfigParams
import com.example.videoapp.MainActivity
import com.example.videoapp.entities.NameEntity
import com.example.videoapp.entities.SubVideoDescriptionEntity
import com.example.videoapp.entities.VideoDescriptionEntity
import com.example.videoapp.entities.VideoDescriptionResponse
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoModel
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.models.Cache.VideoEntityCache
import com.example.videoapp.network.NetworkService
import com.example.videoapp.presenters.VideoDescriptionPresenter
import com.example.videoapp.utils.VideoUtils
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.ywl5320.wlmedia.WlMediaUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class VideoDescriptionModel: VideoModel {
    companion object{
        const val TAG = "VideoDescriptionModel"
        const val JSON_PATH = "all_video_description.json"
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

    fun initVideoDescriptionData (context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            var videoDescriptionResponse : VideoDescriptionResponse? = null
            val job = async {
                videoDescriptionResponse = networkService?.getVideoDescriptionData()
            }
            job.await()

            // 将videoDescriptionResponse写入json
            val videoDescriptionContent: Map<String, List<VideoDescriptionEntity>>? =
                videoDescriptionResponse?.videoDescriptionContent
            if(videoDescriptionContent != null) {
                VideoUtils.saveDescriptionToJson(videoDescriptionContent, File(context.filesDir , JSON_PATH))
            }

            // 展示nameList
            val nameList: List<String>? = videoDescriptionResponse?.nameList
            val nameEntityList= ArrayList<NameEntity>()
            if (nameList != null) {
                for(name in nameList) {
                    nameEntityList.add(NameEntity(name, false))
                }
            }
            (mDescriptionPresenter as VideoDescriptionPresenter).showSelectBar(nameEntityList)
        }
    }

    fun getSelectVideoDescription(context: Context, selectName: String, isDown: Boolean,
                                  blankViewImage: Bitmap, isInit: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val videEntities = ArrayList<VideoEntity>()

            val allVideoDescriptionMap: Map<String, List<VideoDescriptionEntity>> =
                VideoUtils.getJsonToMap(File(context.filesDir , JSON_PATH))

//            allVideoDescriptionMap.forEach {
//                Log.d("czg", "initVideoDescriptionData: videoDescriptionContent key -> " + it.key)
//                val videoDescriptionEntities: List<VideoDescriptionEntity> = it.value
//                for(videoDescriptionEntity in videoDescriptionEntities) {
//                    Log.d(
//                        "czg",
//                        "initVideoDescriptionData: VideoDescriptionEntity title -> " + videoDescriptionEntity.title
//                    )
//                    Log.d(
//                        "czg",
//                        "initVideoDescriptionData: VideoDescriptionEntity imageName -> " + videoDescriptionEntity.imageName
//                    )
//                    val subVideoDescriptionEntities = videoDescriptionEntity.subVideoDescriptionEntities
//                    if (subVideoDescriptionEntities != null) {
//                        for (subVideoDescriptionEntity in subVideoDescriptionEntities) {
//                            Log.d(
//                                "czg",
//                                "initVideoDescriptionData: SubImage -> " + subVideoDescriptionEntity.subImageName
//                            )
//                        }
//                    }
//                }
//            }

            val videoDescriptionEntities =
                allVideoDescriptionMap.getOrDefault(selectName, null)
            if(videoDescriptionEntities != null) {
                var selectId = mMinId - 1
                if(isDown)
                    selectId = mMaxId + 1

                if(selectId >=0 && selectId < videoDescriptionEntities.size) {
                    mMinId = Math.max(0, selectId - ConfigParams.getDescriptionNum / 2)
                    mMaxId = Math.min(videoDescriptionEntities.size - 1, selectId + ConfigParams.getDescriptionNum / 2)
                    for (id in mMinId..mMaxId) {
                        val videoTitle = videoDescriptionEntities[id].title
                        val subVideoDescriptionEntities = videoDescriptionEntities[id].subVideoDescriptionEntities
                        if (!subVideoDescriptionEntities.isNullOrEmpty()) {
                            // TODO 先空之后传输过来
                            var completeUrl: String = ""
                            var videoImage: Bitmap = blankViewImage
                            val videoBitmaps = ArrayList<Pair<String, Bitmap>>()
                            val job = async {
                                for (subVideoDescriptionEnt in subVideoDescriptionEntities) {
                                    completeUrl = ""
                                    val imageBytes =
                                        networkService?.getVideoImageBytes(subVideoDescriptionEnt.subImageName!!)
                                    if (imageBytes != null) {
                                        videoImage = VideoUtils.base64StrToBitmap(imageBytes.get("imageBase64Str")!!)
                                    } else {
                                        videoImage = blankViewImage
                                    }
                                    videoBitmaps.add(Pair(completeUrl, videoImage))
                                }
                            }
                            job.await()
                            // 子视频最后一个视频截图作为总封面
                            val videoEntity = VideoEntity(id, completeUrl, videoTitle!!, videoImage)
                            videoEntity.mBitmapArray = videoBitmaps
                            videEntities.add(videoEntity)
                        }
                    }
                }
            }
            (mDescriptionPresenter as VideoDescriptionPresenter).initVideoInfoRecyclerView(
                isInit, videEntities
            )
        }
    }

    fun updateSelectVideoDescription(context: Context, selectName: String, isDown: Boolean,
                                     blankViewImage: Bitmap, refreshLayout: RefreshLayout,
                                     refreshOperation: (RefreshLayout) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val videEntities = ArrayList<VideoEntity>()

            val allVideoDescriptionMap: Map<String, List<VideoDescriptionEntity>> =
                VideoUtils.getJsonToMap(File(context.filesDir , JSON_PATH))

            val videoDescriptionEntities =
                allVideoDescriptionMap.getOrDefault(selectName, null)
            if(videoDescriptionEntities != null) {
                var selectId = mMinId - 1
                if(isDown)
                    selectId = mMaxId + 1

                if(selectId >=0 && selectId < videoDescriptionEntities.size) {
                    mMinId = Math.max(0, selectId - ConfigParams.getDescriptionNum / 2)
                    mMaxId = Math.min(videoDescriptionEntities.size - 1, selectId + ConfigParams.getDescriptionNum / 2)
                    for (id in mMinId..mMaxId) {
                        val videoTitle = videoDescriptionEntities[id].title
                        val subVideoDescriptionEntities = videoDescriptionEntities[id].subVideoDescriptionEntities
                        if (!subVideoDescriptionEntities.isNullOrEmpty()) {
                            // TODO 先空之后传输过来
                            var completeUrl: String = ""
                            var videoImage: Bitmap = blankViewImage
                            val videoBitmaps = ArrayList<Pair<String, Bitmap>>()
                            val job = async {
                                for (subVideoDescriptionEnt in subVideoDescriptionEntities) {
                                    completeUrl = ""
                                    val imageBytes =
                                        networkService?.getVideoImageBytes(subVideoDescriptionEnt.subImageName!!)
                                    if (imageBytes != null) {
                                        videoImage = VideoUtils.base64StrToBitmap(imageBytes.get("imageBase64Str")!!)
                                    } else {
                                        videoImage = blankViewImage
                                    }
                                    videoBitmaps.add(Pair(completeUrl, videoImage))
                                }
                            }
                            job.await()
                            // 子视频最后一个视频截图作为总封面
                            val videoEntity = VideoEntity(id, completeUrl, videoTitle!!, videoImage)
                            videoEntity.mBitmapArray = videoBitmaps
                            videEntities.add(videoEntity)
                        }
                    }
                }
            }
            (mDescriptionPresenter as VideoDescriptionPresenter).updateVideoInfoRecyclerView(
                videEntities, isDown, refreshLayout, refreshOperation
            )
        }
    }

    fun resetIndex(){
        mMinId = 1
        mMaxId = 23
    }
}