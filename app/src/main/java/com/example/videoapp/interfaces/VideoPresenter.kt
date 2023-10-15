package com.example.videoapp.interfaces

import android.content.Context

interface VideoPresenter {
    // 将model传入presenter，交互（presenter分别于m和v交互）
    fun setModel(model: VideoModel)

    // 将view传入presenter，交互
    fun setView(view: VideoView)
}