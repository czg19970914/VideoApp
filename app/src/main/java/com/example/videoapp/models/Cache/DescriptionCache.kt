package com.example.videoapp.models.Cache

import android.graphics.Bitmap

class DescriptionCache private constructor(): HashMap<Int, Bitmap>() {
    // 这里不应该存Bitmap，需要存Bitmap数组或者直接存entity，直接存entity吧，后面好改
    companion object {
        // 单例模式
        private var instance: DescriptionCache? = null

        fun getInstance (): DescriptionCache? {
            if(instance == null){
                synchronized(DescriptionCache::class.java) {
                    if(instance == null){
                        instance = DescriptionCache()
                    }
                }
            }
            return instance
        }
    }

    override fun put(key: Int, value: Bitmap): Bitmap? {
        // 后续这里可以规定一下缓存的大小，并且根据key优化插入删除元素的算法
        return super.put(key, value)
    }
}