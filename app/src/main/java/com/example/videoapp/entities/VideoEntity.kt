package com.example.videoapp.entities

import android.graphics.Bitmap

class VideoEntity(id: Int, videoUrl: String, videoTitle: String, bitmap: Bitmap) {
    var mId: Int
    var mVideoUrl: String
    var mVideoTitle: String
    var mBitmap: Bitmap

    init {
        mId = id
        mVideoUrl = videoUrl
        mVideoTitle = videoTitle
        mBitmap = bitmap
    }
}