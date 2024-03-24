package com.example.videoapp.models

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.videoapp.ConfigParams
import com.example.videoapp.entities.NameEntity
import com.example.videoapp.entities.SubVideoDescriptionEntity
import com.example.videoapp.entities.VideoDescriptionEntity
import com.example.videoapp.entities.VideoDescriptionResponse
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoModel
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.models.Cache.LRUCache
import com.example.videoapp.network.NetworkService
import com.example.videoapp.presenters.VideoDescriptionPresenter
import com.example.videoapp.utils.VideoUtils
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.*
import java.io.File

class VideoDescriptionModel: VideoModel {
    companion object{
        const val TAG = "VideoDescriptionModel"
        const val JSON_PATH = "all_video_description.json"

        // 只有子视频中超过阈值才并发优化
        const val MULTI_COROUTINES_THRESHOLD = 6

        // 多协程优化的协程数
        const val COROUTINES_NUM = 3

        // 缓存图片cache的大小
        const val BITMAP_CACHE_SIZE = 400
    }

    private var mDescriptionPresenter: VideoPresenter? = null

    private var mMinId = 1
    private var mMaxId = 23

    private val networkService: NetworkService = NetworkService.createService()

    private val mBitmapCache: LRUCache = LRUCache(BITMAP_CACHE_SIZE)

    override fun setPresenter(presenter: VideoPresenter) {
        mDescriptionPresenter = presenter
    }

    fun initVideoDescriptionData (context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            var videoDescriptionResponse : VideoDescriptionResponse? = null

            try {
                videoDescriptionResponse = networkService.getVideoDescriptionData()
            } catch (e: Exception) {
                Log.i(TAG, "initVideoDescriptionData: 网络获取错误")
            }

            // 将videoDescriptionResponse写入json
            val videoDescriptionContent: Map<String, List<VideoDescriptionEntity>>? =
                videoDescriptionResponse?.videoDescriptionContent
            if(videoDescriptionContent != null) {
                VideoUtils.saveDescriptionToJson(videoDescriptionContent, File(context.filesDir , JSON_PATH))
            } else {
                Log.i(TAG, "initVideoDescriptionData: 获取的json数据为空")
            }

            // 展示nameList
            val nameList: List<String>? = videoDescriptionResponse?.nameList
            val nameEntityList= ArrayList<NameEntity>()
            if (nameList != null) {
                for(name in nameList) {
                    nameEntityList.add(NameEntity(name, false))
                }
            } else {
                Log.i(TAG, "initVideoDescriptionData: 展示的nameList为空")
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

            val videoDescriptionEntities =
                allVideoDescriptionMap.getOrDefault(selectName, null)
            if(videoDescriptionEntities != null) {
                var selectId = mMinId - 1
                if(isDown)
                    selectId = mMaxId + 1

                if(selectId >=0 && selectId < videoDescriptionEntities.size) {
                    mMinId = 0.coerceAtLeast(selectId - ConfigParams.getDescriptionNum / 2 + 1)
                    mMaxId =
                        (videoDescriptionEntities.size - 1).coerceAtMost(selectId + ConfigParams.getDescriptionNum / 2)
                    for (id in mMinId..mMaxId) {
                        val videoTitle = videoDescriptionEntities[id].title
                        val subVideoDescriptionEntities = videoDescriptionEntities[id].subVideoDescriptionEntities
                        if (!subVideoDescriptionEntities.isNullOrEmpty()) {
                            val videoBitmaps = ArrayList<Pair<String, Bitmap>>()

                            // 并行优化
                            // 暂存需要网络请求的实体
                            val networkSubVideoDescriptionEntities = mutableListOf<SubVideoDescriptionEntity>()
                            for (index in subVideoDescriptionEntities.indices) {
                                val key = subVideoDescriptionEntities[index].subVideoPath
                                if(key.isNullOrEmpty()) {
                                    continue
                                }
                                val completeUrl =
                                    ConfigParams.baseUrl + "videoPlay?file_name=" + subVideoDescriptionEntities[index].subVideoPath
                                val cacheImage = mBitmapCache.get(key)
                                if(cacheImage != null) {
                                    videoBitmaps.add(Pair(completeUrl, cacheImage))
                                } else {
                                    networkSubVideoDescriptionEntities.add(subVideoDescriptionEntities[index])
                                }
                            }
                            val networkSubVideoDescriptionList = networkSubVideoDescriptionEntities.toList()
                            if (networkSubVideoDescriptionList.size < MULTI_COROUTINES_THRESHOLD) {
                                getSubVideoDescriptionTask(networkSubVideoDescriptionList, videoBitmaps,
                                    blankViewImage, 0, networkSubVideoDescriptionList.size,
                                    networkSubVideoDescriptionList.size)
                            } else {
                                val taskLen = (networkSubVideoDescriptionList.size / COROUTINES_NUM) + 1
                                val deferredList = (0 until COROUTINES_NUM).map {
                                    async {
                                        getSubVideoDescriptionTask(networkSubVideoDescriptionList,
                                            videoBitmaps, blankViewImage,
                                            it*taskLen, (it+1)*taskLen,
                                            networkSubVideoDescriptionList.size)
                                    }
                                }
                                deferredList.awaitAll()
                            }

                            // 子视频第一个视频截图作为总封面
                            if(videoBitmaps.size > 0) {
                                val videoEntity = VideoEntity(
                                    id, videoBitmaps[0].first, videoTitle!!, videoBitmaps[0].second)
                                videoEntity.mBitmapArray = videoBitmaps
                                videEntities.add(videoEntity)
                            }
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
                    if(selectId % 2 == 0) {
                        mMinId = 0.coerceAtLeast(selectId - ConfigParams.getDescriptionNum / 2 + 1)
                        mMaxId =
                            (videoDescriptionEntities.size - 1).coerceAtMost(selectId + ConfigParams.getDescriptionNum / 2)
                    } else {
                        mMinId = 0.coerceAtLeast(selectId - ConfigParams.getDescriptionNum / 2)
                        mMaxId =
                            (videoDescriptionEntities.size - 1).coerceAtMost(selectId + ConfigParams.getDescriptionNum / 2 - 1)
                    }
                    for (id in mMinId..mMaxId) {
                        val videoTitle = videoDescriptionEntities[id].title
                        val subVideoDescriptionEntities = videoDescriptionEntities[id].subVideoDescriptionEntities
                        if (!subVideoDescriptionEntities.isNullOrEmpty()) {
                            val videoBitmaps = ArrayList<Pair<String, Bitmap>>()

                            // 并行优化
                            // 暂存需要网络请求的实体
                            val networkSubVideoDescriptionEntities = mutableListOf<SubVideoDescriptionEntity>()
                            for (index in subVideoDescriptionEntities.indices) {
                                val key = subVideoDescriptionEntities[index].subVideoPath
                                if(key.isNullOrEmpty()) {
                                    continue
                                }
                                val completeUrl =
                                    ConfigParams.baseUrl + "videoPlay?file_name=" + subVideoDescriptionEntities[index].subVideoPath
                                val cacheImage = mBitmapCache.get(key)
                                if(cacheImage != null) {
                                    videoBitmaps.add(Pair(completeUrl, cacheImage))
                                } else {
                                    networkSubVideoDescriptionEntities.add(subVideoDescriptionEntities[index])
                                }
                            }
                            val networkSubVideoDescriptionList = networkSubVideoDescriptionEntities.toList()
                            if (networkSubVideoDescriptionList.size < MULTI_COROUTINES_THRESHOLD) {
                                getSubVideoDescriptionTask(networkSubVideoDescriptionList, videoBitmaps,
                                    blankViewImage, 0, networkSubVideoDescriptionList.size,
                                    networkSubVideoDescriptionList.size)
                            } else {
                                val taskLen = (networkSubVideoDescriptionList.size / COROUTINES_NUM) + 1
                                val deferredList = (0 until COROUTINES_NUM).map {
                                    async {
                                        getSubVideoDescriptionTask(networkSubVideoDescriptionList,
                                            videoBitmaps, blankViewImage,
                                            it*taskLen, (it+1)*taskLen,
                                            networkSubVideoDescriptionList.size)
                                    }
                                }
                                deferredList.awaitAll()
                            }

                            // 子视频第一个视频截图作为总封面
                            if(videoBitmaps.size > 0) {
                                val videoEntity = VideoEntity(
                                    id, videoBitmaps[0].first, videoTitle!!, videoBitmaps[0].second)
                                videoEntity.mBitmapArray = videoBitmaps
                                videEntities.add(videoEntity)
                            }
                        }
                    }
                }
            }
            (mDescriptionPresenter as VideoDescriptionPresenter).updateVideoInfoRecyclerView(
                videEntities, isDown, refreshLayout, refreshOperation
            )
        }
    }

    // 将获取subVideoDescription分成几段完成并行优化的执行
    private suspend fun getSubVideoDescriptionTask(
        subVideoDescriptionEntities: List<SubVideoDescriptionEntity>,
        videoBitmaps: ArrayList<Pair<String, Bitmap>>, blankViewImage: Bitmap,
        start: Int, end: Int, maxLen: Int) {
        val startIndex = 0.coerceAtLeast(start)
        val endIndex = maxLen.coerceAtMost(end)
        for(index in startIndex until endIndex) {
            if(subVideoDescriptionEntities[index].subVideoPath.isNullOrEmpty() ||
                subVideoDescriptionEntities[index].subImageName.isNullOrEmpty()) {
                continue
            }
            val completeUrl =
                ConfigParams.baseUrl + "videoPlay?file_name=" + subVideoDescriptionEntities[index].subVideoPath
            val imageBytes =
                networkService.getVideoImageBytes(subVideoDescriptionEntities[index].subImageName!!)
            var videoImage: Bitmap = blankViewImage
            try {
                videoImage = VideoUtils.base64StrToBitmap(imageBytes["imageBase64Str"]!!)
            } catch (e: Exception) {
                Log.i(TAG, "getSelectVideoDescription: 网络获取错误")
            }
            synchronized(this) {
                mBitmapCache.set(subVideoDescriptionEntities[index].subVideoPath!!, videoImage)
            }
            videoBitmaps.add(Pair(completeUrl, videoImage))
        }
    }

    fun resetIndex(){
        mMinId = 1
        mMaxId = 23
    }
}