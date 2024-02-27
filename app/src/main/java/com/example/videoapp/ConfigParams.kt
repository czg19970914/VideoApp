package com.example.videoapp

class ConfigParams {
    companion object {
//        @JvmStatic
//        val baseUrl = "http://192.168.0.111:8080/videos/"
        @JvmStatic
        val baseUrl = "http://10.234.209.87:8080/"

        @JvmStatic
        val viewHeightOffset = 150 // 由于只能获取设计屏幕的宽、高，由于上下的bar存在需要调整TextureView高度

        @JvmStatic
        val getDescriptionNum = 22 // 每次加载图片信息的个数
    }
}