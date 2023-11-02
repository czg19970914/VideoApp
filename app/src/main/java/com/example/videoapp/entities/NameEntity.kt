package com.example.videoapp.entities

class NameEntity(nameContent: String, isChoose: Boolean) {
    var mNameContent: String
    var mIsChoose: Boolean

    init {
        mNameContent = nameContent
        mIsChoose = isChoose
    }
}