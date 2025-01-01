package com.example.videoapp.entities

import android.graphics.Bitmap

class VideoEntity(id: Int, videoUrl: String, videoTitle: String, bitmap: Bitmap) {
    var mId: Int = id
    var mVideoUrl: String = videoUrl
    var mVideoTitle: String = videoTitle
    var mBitmap: Bitmap = bitmap

    // 在新的版本中，mVideoUrl应该是一个字符串数组，在一个文件夹下有多个链接
    var mBitmapArray: ArrayList<Pair<String, Bitmap>>? = null
}