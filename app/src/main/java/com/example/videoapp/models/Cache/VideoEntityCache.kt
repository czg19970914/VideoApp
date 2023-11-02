package com.example.videoapp.models.Cache

import com.example.videoapp.entities.VideoEntity

class VideoEntityCache private constructor(): HashMap<String, VideoEntity>() {
    companion object{
        // 单例模式
        private var instance: VideoEntityCache? = null

        fun getInstance (): VideoEntityCache? {
            if(instance == null){
                synchronized(VideoEntityCache::class.java) {
                    if(instance == null){
                        instance = VideoEntityCache()
                    }
                }
            }
            return instance
        }
    }

    override fun put(key: String, value: VideoEntity): VideoEntity? {
        // 后续这里可以规定一下缓存的大小，并且根据key优化插入删除元素的算法
        // 这里key为“name_id”,到时候写算法
        return super.put(key, value)
    }
}