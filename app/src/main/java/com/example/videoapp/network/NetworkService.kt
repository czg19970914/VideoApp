package com.example.videoapp.network

import com.example.videoapp.entities.VideoDescriptionResponse
import retrofit2.http.GET

interface NetworkService {
    @GET("/videoDescriptionData")
    suspend fun getVideoDescriptionData() : VideoDescriptionResponse

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