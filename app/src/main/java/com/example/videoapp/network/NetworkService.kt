package com.example.videoapp.network

import com.example.videoapp.entities.VideoDescriptionEntity
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkService {

    @GET("/descriptionNameList")
    suspend fun getVideoDescriptionBarNames() : List<String>

    @GET(value = "/videoDescription")
    suspend fun getVideoDescriptionMap(
        @Query("select_name") selectName: String,
        @Query("min_id") minId: Int,
        @Query("max_id") maxId: Int
    ): Map<String, VideoDescriptionEntity>

    /**
     * TODO 通过 Retrofit 创建一个 NetworkService 实例
     */
    companion object{
        fun createService() : NetworkService {
            return NetworkModule.createRetrofit(NetworkModule.createOkHttpClient())
                .create(NetworkService::class.java) // TODO 返回一个 NetworkService 的实例
        }
    }
}