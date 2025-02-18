package com.example.videoapp.utils

import android.content.Context
import android.os.Build
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi

class ScreenUtils {
    companion object {
        const val TAG = "ScreenUtils"

        @JvmStatic
        fun getWindowWidth(context: Context): Int {
            val windowManager: WindowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val windowBounds = windowMetrics.bounds
            return windowBounds.width()
        }

        @JvmStatic
        fun getWindowHeight(context: Context): Int {
            val windowManager: WindowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val windowBounds = windowMetrics.bounds
            return windowBounds.height()
        }
    }
}