package com.example.videoapp.entities

import android.graphics.Bitmap

class VideoEntity(videoUrl: String, videoTitle: String, bitmap: Bitmap) {
    var mVideoUrl: String
    var mVideoTitle: String
    var mBitmap: Bitmap

    init {
        mVideoUrl = videoUrl
        mVideoTitle = videoTitle
        mBitmap = bitmap
    }
}