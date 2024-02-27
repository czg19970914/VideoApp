package com.example.videoapp.network

import com.example.videoapp.entities.VideoDescriptionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkService {
    @GET("/videoDescriptionData")
    suspend fun getVideoDescriptionData() : VideoDescriptionResponse

    @GET("/videoImageBytes")
    suspend fun getVideoImageBytes(
        @Query(value = "image_file_path") imageFilePath : String
    ): Map<String, String>

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