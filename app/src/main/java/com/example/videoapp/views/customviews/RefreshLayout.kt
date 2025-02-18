package com.example.videoapp.views.customviews

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R


/**
 * 可以上拉下拉刷新数据的Layout，里面包裹一个RecyclerView
 * 注意：是定制的Layout，里面只能存放一个RecyclerView
 */
class RefreshLayout: ViewGroup {
    companion object {
        const val TAG = "RefreshLayout"

        // 拖动开始更新数据的手势阈值
        const val START_LOAD_THRESHOLD = 100L

        // loading界面所需要的高度
        const val LOADING_VIEW_HEIGHT = 80

        // 给布局底部加一些padding
        const val PADDING_BOTTOM = 80
    }
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    private var mIsLoading: Boolean = false
    private var mIsUpdateDown: Boolean = false
    private var mIsUpdateUp: Boolean = false
    private var mStartX: Float = 0f
    private var mStartY: Float = 0f

    private val mLoadingView: View = LayoutInflater.from(this.context).inflate(R.layout.loading_item, null)

    private var mLoadMoreListener: LoadMorListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val childCount = childCount
        if (childCount > 2) {
            return
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = childCount
        if (childCount > 2) {
            return
        }
        val bottom = b - PADDING_BOTTOM
        when (childCount) {
            1 -> {
                val child = getChildAt(0)
                if (child.visibility != GONE && child is RecyclerView) {
                    child.layout(0, 0, r, bottom)
                }
            }
            2 -> {
                if (mIsLoading) {
                    val child1 = getChildAt(0)
                    val child2 = getChildAt(1)
                    val recyclerView: RecyclerView
                    val loadView: View
                    if (child1 is RecyclerView) {
                        recyclerView = child1
                        loadView = child2
                    } else if (child2 is RecyclerView) {
                        recyclerView = child2
                        loadView = child1
                    } else {
                        return
                    }
                    recyclerView.layout(0, 0, r, bottom)
                    if (mIsUpdateDown) {
                        loadView.layout(0, bottom - LOADING_VIEW_HEIGHT, r, bottom)
                    } else if (mIsUpdateUp) {
                        loadView.layout(0, 0, r, LOADING_VIEW_HEIGHT)
                    }
                } else {
                    Log.i(TAG, "onLayout: state is error!")
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val childCount = childCount
        if (childCount > 2) {
            return super.dispatchTouchEvent(ev)
        }
        val action = ev?.actionMasked
        action?.let {
            when (action) {
                MotionEvent.ACTION_MOVE -> {
                    if (mIsLoading) {
                        return false
                    }
                    if (childCount == 1) {
                        val child = getChildAt(0)
                        if (child.visibility != GONE && child is RecyclerView) {
                            if (!child.canScrollVertically(1)) {
                                if (!mIsUpdateDown) {
                                    mStartY = ev.y
                                    mIsUpdateDown = true
                                }
                            } else if (!child.canScrollVertically(-1)) {
                                if (!mIsUpdateUp) {
                                    mStartY = ev.y
                                    mIsUpdateUp = true
                                }
                            } else {
                                mIsUpdateDown = false
                                mIsUpdateUp = false
                            }
                            if ((mIsUpdateDown && mStartY - ev.y > START_LOAD_THRESHOLD) ||
                                (mIsUpdateUp && ev.y - mStartY > START_LOAD_THRESHOLD)) {
                                mIsLoading = true
                                addLoadView()
                                if (mIsUpdateDown) {
                                    mLoadMoreListener?.loadDownMore()
                                } else if (mIsUpdateUp) {
                                    mLoadMoreListener?.loadUpMore()
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun finishLoadMode() {
        if (mIsLoading) {
            removeLoadView()
            mIsLoading = false
        }
    }

    private fun addLoadView() {
        addView(mLoadingView)
    }

    private fun removeLoadView() {
        removeView(mLoadingView)
    }

    fun setLoadMoreListener(loadMorListener: LoadMorListener) {
        mLoadMoreListener = loadMorListener
    }

    interface LoadMorListener {
        fun loadDownMore()

        fun loadUpMore()
    }

}