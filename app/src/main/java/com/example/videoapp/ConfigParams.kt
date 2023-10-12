package com.example.videoapp

class ConfigParams {
    companion object {
        @JvmStatic
        val baseUrl = "http://192.168.0.108:8080/videos/"

        @JvmStatic
        var viewHeightOffset = 150 // 由于只能获取设计屏幕的宽、高，由于上下的bar存在需要调整TextureView高度
    }
}