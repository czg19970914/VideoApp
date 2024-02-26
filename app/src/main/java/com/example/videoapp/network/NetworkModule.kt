package com.example.videoapp.network

import com.example.videoapp.ConfigParams
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    /**
     * TODO 创建 Retrofit 实例
     */
    fun createRetrofit(okHttpClient: OkHttpClient) : Retrofit {
        // 返回一个 retrofit 实例
        return Retrofit.Builder()
            .client(okHttpClient) // 让 retrofit 使用 okhttp
            .baseUrl(ConfigParams.baseUrl) // api 地址
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))// 使用 gson 解析 json
            .build()
    }

    /**
     * TODO 创建 OkHttpClient 实例
     */
    fun createOkHttpClient() : OkHttpClient{
        // 返回一个 OkHttpClient 实例
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)// 设置连接超时时间
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .build()
    }
}