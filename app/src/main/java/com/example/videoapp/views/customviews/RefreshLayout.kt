package com.example.videoapp.views.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R


/**
 * 可以上拉下拉刷新数据的Layout，里面包裹一个RecyclerView和一个LoadView
 * 注意：是定制的Layout，里面只能存放一个RecyclerView和一个LoadView
 */
class RefreshLayout: ViewGroup {
    companion object {
        const val TAG = "RefreshLayout"

        // 拖动开始更新数据的手势阈值
        const val START_LOAD_THRESHOLD = 100L

        // 给布局底部加一些padding
        const val PADDING_BOTTOM = 150
    }
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    private var mIsLoading: Boolean = false
    private var mIsUpdateDown: Boolean = false
    private var mIsUpdateUp: Boolean = false
    private var mStartX: Float = 0f
    private var mStartY: Float = 0f
    private var mLoadViewHeight: Int = 0

    private var mCanShowNoMoreToast: Boolean = true

    private val mLoadingView: View = LayoutInflater.from(this.context).inflate(R.layout.loading_item, null)

    private var mLoadMoreListener: LoadMorListener? = null

    init {
        mLoadingView.visibility = GONE
        addView(mLoadingView, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val childCount = childCount
        if (childCount != 2) {
            return
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = childCount
        if (childCount != 2) {
            return
        }
        val bottom = b - PADDING_BOTTOM
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
            if (mIsLoading) {
                loadView.layout(0, bottom - START_LOAD_THRESHOLD.toInt(), r, bottom)
            } else {
                loadView.layout(0, bottom - mLoadViewHeight, r, bottom)
            }
        } else if (mIsUpdateUp) {
            if (mIsLoading) {
                loadView.layout(0, 0, r, START_LOAD_THRESHOLD.toInt())
            } else {
                loadView.layout(0, 0, r, mLoadViewHeight)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val childCount = childCount
        if (childCount != 2) {
            return super.dispatchTouchEvent(ev)
        }
        val action = ev?.actionMasked
        action?.let {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    mCanShowNoMoreToast = true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mIsLoading) {
                        return false
                    }
                    val child1 = getChildAt(0)
                    val child2 = getChildAt(1)
                    val recyclerView: RecyclerView
                    if (child1 is RecyclerView) {
                        recyclerView = child1
                    } else if (child2 is RecyclerView) {
                        recyclerView = child2
                    } else {
                        return super.dispatchTouchEvent(ev)
                    }
                    if (recyclerView.visibility != GONE) {
                        if (!recyclerView.canScrollVertically(1)) {
                            if (!canLoadingMore(1)) {
                                return super.dispatchTouchEvent(ev)
                            }
                            if (!mIsUpdateDown) {
                                mStartY = ev.y
                                mIsUpdateDown = true
                                showLoadView()
                            }
                        } else if (!recyclerView.canScrollVertically(-1)) {
                            if (!canLoadingMore(-1)) {
                                return super.dispatchTouchEvent(ev)
                            }
                            if (!mIsUpdateUp) {
                                mStartY = ev.y
                                mIsUpdateUp = true
                                showLoadView()
                            }
                        } else {
                            dismissLoadView()
                            mCanShowNoMoreToast = true
                        }
                        if (mIsUpdateDown) {
                            if (mStartY - ev.y > START_LOAD_THRESHOLD) {
                                mIsLoading = true
                                mLoadMoreListener?.loadDownMore()
                            }
                            mLoadViewHeight = calculateLoadViewHeight(mStartY, ev.y,
                                START_LOAD_THRESHOLD.toInt(), true)
                            requestLayout()
                        } else if (mIsUpdateUp) {
                            if (ev.y - mStartY > START_LOAD_THRESHOLD) {
                                mIsLoading = true
                                mLoadMoreListener?.loadUpMore()
                            }
                            mLoadViewHeight = calculateLoadViewHeight(mStartY, ev.y,
                                START_LOAD_THRESHOLD.toInt(), false)
                            requestLayout()
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (!mIsLoading) {
                        dismissLoadView()
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    if (!mIsLoading) {
                        dismissLoadView()
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun canLoadingMore(direction: Int): Boolean {
        if (mLoadMoreListener?.canLoadMore(direction) == false) {
            dismissLoadView(false)
            if (mLoadViewHeight > 10) {
                if (mCanShowNoMoreToast) {
                    Toast.makeText(
                        this.context, this.resources.getString(
                            R.string.no_more_data_loading_text
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                    mCanShowNoMoreToast = false
                }
                return false
            }
        }
        return true
    }
    fun finishLoadMode() {
        if (mIsLoading) {
            dismissLoadView()
        }
    }

    fun setLoadMoreListener(loadMorListener: LoadMorListener) {
        mLoadMoreListener = loadMorListener
    }

    private fun showLoadView() {
        mLoadingView.visibility = VISIBLE
        mLoadingView.bringToFront()
    }

    private fun dismissLoadView(needResetFlag: Boolean = true) {
        mLoadingView.visibility = GONE
        mIsLoading = false
        if (needResetFlag) {
            mIsUpdateDown = false
            mIsUpdateUp = false
            mLoadViewHeight = 0
        }
    }

    private fun calculateLoadViewHeight(startY: Float, currentY: Float,
                                        maxHeight: Int, isDown: Boolean): Int {
        val moveY = if (isDown) {
            (startY - currentY).toInt()
        } else {
            (currentY - startY).toInt()
        }
        var loadViewHeight = 0.coerceAtLeast(moveY)
        loadViewHeight = loadViewHeight.coerceAtMost(maxHeight)
        return loadViewHeight
    }

    fun onDestroy() {
        mLoadMoreListener = null
    }

    interface LoadMorListener {
        fun loadDownMore()

        fun loadUpMore()

        fun canLoadMore(direction: Int): Boolean
    }
}