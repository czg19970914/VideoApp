package com.example.videoapp.interfaces

interface VideoView {
    // 将控制器传入到view，view只与presenter交互
    fun setPresenter(presenter: VideoPresenter)
}