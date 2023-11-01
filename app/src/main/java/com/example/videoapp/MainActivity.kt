package com.example.videoapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.interfaces.VideoView
import com.example.videoapp.presenters.VideoDescriptionPresenter
import com.example.videoapp.utils.VideoUtils
import com.example.videoapp.views.recyclerviews.DetailRecyclerViewAdapter
import com.example.videoapp.views.recyclerviews.VideoRecyclerViewAdapter
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.*

import com.example.videoapp.views.recyclerviews.VideoRecyclerViewAdapter.OnImageClickListener


class MainActivity : AppCompatActivity(), VideoView {
    private val mRefreshLayout: SmartRefreshLayout by lazy {
        findViewById(R.id.smart_refresh_layout)
    }
    private val mVideoRecyclerView: RecyclerView by lazy {
        findViewById(R.id.video_recycler_view)
    }
    private val mLeftMenu: RelativeLayout by lazy {
        findViewById(R.id.left_menu)
    }
    private val mLeftMenuCancel: ImageView by lazy {
        findViewById(R.id.left_menu_cancel)
    }
    private val mDetailRecyclerView: RecyclerView by lazy {
        findViewById(R.id.left_menu_recycler_view)
    }

    private var mWaitingDialogBuilder: AlertDialog.Builder? = null
    private var mWaitingDialog: AlertDialog? = null

    private var mVideoListLayoutManager: GridLayoutManager? = null
    private var mVideoListAdapter: VideoRecyclerViewAdapter? = null

    private var mDetailListLayoutManager: LinearLayoutManager? = null
    private var mDetailListAdapter: DetailRecyclerViewAdapter? = null

    private var mDescriptionPresenter: VideoPresenter

    init {
        mDescriptionPresenter = VideoDescriptionPresenter()
        (mDescriptionPresenter as VideoDescriptionPresenter).setView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.light_gray)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.light_gray)
        initRefreshLayout()
        initWaitingDialog()

        showWaitingDialog()
        (mDescriptionPresenter as VideoDescriptionPresenter).initServerData()

        mLeftMenuCancel.setOnClickListener {
            closeLeftMenu()
        }
        initDetailRecyclerView()
    }

    private fun initRecyclerView(videoEntities: ArrayList<VideoEntity>) {
        mVideoListAdapter = VideoRecyclerViewAdapter(this, videoEntities)
        mVideoListAdapter!!.setImageClickListener(object: OnImageClickListener{
            override fun showLeftMenu(detailData: ArrayList<VideoEntity>) {
                openLeftMenu(detailData)
            }

        })

        mVideoListLayoutManager = GridLayoutManager(this, 2)
        mVideoRecyclerView.layoutManager = mVideoListLayoutManager
        mVideoRecyclerView.adapter = mVideoListAdapter
    }

    private fun initDetailRecyclerView() {
        val blankView = VideoUtils.vectorDrawableToBitmap(this, R.drawable.blank_video_image)
        val detailData = ArrayList<VideoEntity>()
        detailData.add(VideoEntity(1, "dfdf", "test", blankView))
        detailData.add(VideoEntity(2, "dfdf", "test", blankView))
        detailData.add(VideoEntity(3, "dfdf", "test", blankView))
        detailData.add(VideoEntity(4, "dfdf", "test", blankView))
        detailData.add(VideoEntity(5, "dfdf", "test", blankView))
        detailData.add(VideoEntity(6, "dfdf", "test", blankView))
        detailData.add(VideoEntity(7, "dfdf", "test", blankView))

        mDetailListAdapter = DetailRecyclerViewAdapter(this, detailData)
        mDetailListLayoutManager = LinearLayoutManager(this)
        mDetailListLayoutManager?.orientation = LinearLayoutManager.HORIZONTAL
        mDetailRecyclerView.layoutManager = mDetailListLayoutManager
        mDetailRecyclerView.adapter = mDetailListAdapter
    }

    private fun initWaitingDialog(){
        mWaitingDialogBuilder = AlertDialog.Builder(this)
        mWaitingDialogBuilder!!.setCancelable(false)
        mWaitingDialogBuilder!!.setView(R.layout.dialog_waiting)
    }

    private fun initRefreshLayout() {
        //设置头部刷新的样式
        mRefreshLayout.setRefreshHeader(ClassicsHeader(this))
        //设置页脚刷新的样式
        mRefreshLayout.setRefreshFooter(ClassicsFooter(this))
        //设置头部刷新时间监听
        mRefreshLayout.setOnRefreshListener { it ->
            (mDescriptionPresenter as VideoDescriptionPresenter).updateServerData(false,
                it
            ) { refreshLayout -> refreshLayout.finishRefresh() }
        }
        //设置尾部刷新时间监听
        mRefreshLayout.setOnLoadMoreListener {
            (mDescriptionPresenter as VideoDescriptionPresenter).updateServerData(true,
                it
            ) { refreshLayout -> refreshLayout.finishLoadMore() }
        }
    }

    suspend fun showVideoInfoRecyclerView(videoEntities: ArrayList<VideoEntity>)= withContext(Dispatchers.Main) {
        initRecyclerView(videoEntities)
        closeWaitingDialog()
    }

    suspend fun updateVideoInfoRecyclerView(videoEntities: ArrayList<VideoEntity>, isDown: Boolean,
                                            refreshLayout: RefreshLayout, refreshOperation: (RefreshLayout) -> Unit)
    = withContext(Dispatchers.Main) {
        if(videoEntities.size == 0){

        }else{
            mVideoListAdapter?.updateVideoDescription(videoEntities)
            if(isDown)
                mVideoListLayoutManager?.scrollToPosition(0)
            else
                mVideoListLayoutManager?.scrollToPosition(
                    mVideoListAdapter!!.itemCount.coerceAtMost(ConfigParams.getDescriptionNum - 1)
                )
        }
        refreshOperation(refreshLayout)
    }

    private fun showWaitingDialog() {
        mWaitingDialog = mWaitingDialogBuilder?.show()
    }
    private fun closeWaitingDialog() {
        mWaitingDialog?.dismiss()
    }

    override fun setPresenter(presenter: VideoPresenter) {
        mDescriptionPresenter = presenter
    }

    fun openLeftMenu(detailData: ArrayList<VideoEntity>){
        mDetailListAdapter?.updateDetailData(detailData)

        mLeftMenu.visibility = View.VISIBLE
    }
    fun closeLeftMenu(){
        mLeftMenu.visibility = View.GONE
    }
}