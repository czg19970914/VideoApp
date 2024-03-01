package com.example.videoapp.models.Cache

import android.graphics.Bitmap

/**
 *
 * 双向链表
 * **/
class DLinkNode {
    var key: String? = null
    var videoImage: Bitmap? = null
    var preNode: DLinkNode? = null
    var postNode: DLinkNode? = null
}