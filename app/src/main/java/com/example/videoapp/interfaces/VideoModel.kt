package com.example.videoapp.interfaces

interface VideoModel {

    // 将控制器传入到model，model只与presenter交互
    fun setPresenter(presenter: VideoPresenter)
}