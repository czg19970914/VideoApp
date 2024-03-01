package com.example.videoapp.models.Cache

import android.graphics.Bitmap
import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

/**
 * 使用LRU算法来缓存图片，减少网络申请
 * **/
class LRUCache(private val size: Int){

    private var count: AtomicInteger
    private val cache: HashMap<String, DLinkNode>
    private val headNode: DLinkNode
    private val tailNode: DLinkNode

    init {
        cache = HashMap<String, DLinkNode>()

        count = AtomicInteger(0)

        headNode = DLinkNode()
        headNode.preNode = null

        tailNode = DLinkNode()
        tailNode.postNode = null

        headNode.postNode = tailNode
        tailNode.preNode = headNode
    }

    /**
     * 增加一个节点到链表，并且在总是头节点之后
     */
    private fun addNode(node: DLinkNode) {
        node.preNode = headNode
        node.postNode = headNode.postNode

        headNode.postNode?.preNode = node
        headNode.postNode = node
    }

    /**
     * 删除一个已经存在的节点
     */
    private fun removeNode(node: DLinkNode) {
        val pre = node.preNode
        val post = node.postNode

        pre?.postNode = post
        post?.preNode = pre
    }

    /**
     * 将节点移动到最前面
     */
    private fun moveToHead(node: DLinkNode) {
        this.removeNode(node)
        this.addNode(node)
    }

    /**
     * 弹出最靠近尾巴的节点
     */
    private fun popTail(): DLinkNode {
        val res = tailNode.preNode
        this.removeNode(res!!)

        return res
    }

    fun get(key: String): Bitmap? {
        val node = cache[key] ?: return null
        this.moveToHead(node)
        return node.videoImage
    }

    fun set(key: String, value: Bitmap) {
        Log.i("czg", "set: " + count.get())
        val node = cache[key]
        if(node == null) {
            val newNode = DLinkNode()
            newNode.key = key
            newNode.videoImage = value
            this.cache[key] = newNode
            this.addNode(newNode)
            count.incrementAndGet()

            if(count.get() > size) {
                val tail = this.popTail()
                this.cache.remove(tail.key)
                count.decrementAndGet()
            }
        }else {
            node.videoImage = value
            this.moveToHead(node)
        }
    }
}