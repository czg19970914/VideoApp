package com.example.videoapp.network

import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkService {

    @GET("/descriptionNameList")
    suspend fun getVideoDescriptionBarNames() : List<String>

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