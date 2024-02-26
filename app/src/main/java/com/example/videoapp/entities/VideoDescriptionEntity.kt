package com.example.videoapp.entities

class VideoDescriptionEntity {
    // 该视频的标题
    val title: String? = null

    // 子视频的文件路径和缩略图的base64字符串
    var subImages: MutableList<SubVideoDescriptionEntity?>? = null
}