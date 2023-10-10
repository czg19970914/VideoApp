package com.example.videoapp.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask

open class VideoAsyncTask(context: Context): AsyncTask<Unit, Unit, Unit>() {
    @SuppressLint("StaticFieldLeak")
    val mContext: Context
    var mBitmap: Bitmap? = null

    init {
        mContext = context
    }
    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg p0: Unit?) {

    }
}