package com.example.videoapp

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.entities.NameEntity
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.interfaces.VideoPresenter
import com.example.videoapp.interfaces.VideoView
import com.example.videoapp.presenters.VideoDescriptionPresenter
import com.example.videoapp.views.recyclerviews.DetailRecyclerViewAdapter
import com.example.videoapp.views.recyclerviews.SelectBarAdapter
import com.example.videoapp.views.recyclerviews.VideoRecyclerViewAdapter
import com.example.videoapp.views.recyclerviews.VideoRecyclerViewAdapter.OnImageClickListener
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(), VideoView, SelectBarAdapter.OnSelectBarClickListener {
    private val mSelectNameBar: RecyclerView by lazy {
        findViewById(R.id.select_name_bar)
    }
    private val mRefreshLayout: SmartRefreshLayout by lazy {
        findViewById(R.id.smart_refresh_layout)
    }
    private val mVideoRecyclerView: RecyclerView by lazy {
        findViewById(R.id.video_recycler_view)
    }
    private val mLeftMenu: RelativeLayout by lazy {
        findViewById(R.id.left_menu)
    }
    private val mLeftMenuContent: LinearLayout by lazy {
        findViewById(R.id.left_menu_content)
    }
    private val mLeftMenuCancel: ImageView by lazy {
        findViewById(R.id.left_menu_cancel)
    }
    private val mDetailRecyclerView: RecyclerView by lazy {
        findViewById(R.id.left_menu_recycler_view)
    }

    private var mSelectBarLayoutManager: LinearLayoutManager? = null
    private var mSelectBarAdapter: SelectBarAdapter? = null

    private var mWaitingDialogBuilder: AlertDialog.Builder? = null
    private var mWaitingDialog: AlertDialog? = null

    private var mVideoListLayoutManager: GridLayoutManager? = null
    private var mVideoListAdapter: VideoRecyclerViewAdapter? = null

    private var mDetailListLayoutManager: LinearLayoutManager? = null
    private var mDetailListAdapter: DetailRecyclerViewAdapter? = null

    private var mDescriptionPresenter: VideoPresenter

    private var mIsInUpdate = false // 是否当前正在刷新，默认不是

    init {
        mDescriptionPresenter = VideoDescriptionPresenter()
        (mDescriptionPresenter as VideoDescriptionPresenter).setView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar)
        ViewCompat.getWindowInsetsController(window.decorView)?.isAppearanceLightStatusBars = true
        window.navigationBarColor = ContextCompat.getColor(this, R.color.main_background)
        initWaitingDialog()

        showWaitingDialog()
        (mDescriptionPresenter as VideoDescriptionPresenter).getNameList()

        mLeftMenuCancel.setOnClickListener {
            val rotateAnimator = RotateAnimation(0f, 180f,
                it.width / 2.toFloat(), it.height / 2.toFloat())
            rotateAnimator.duration = 500
            it.startAnimation(rotateAnimator)

            closeLeftMenu()
        }
        initDetailRecyclerView()
    }

    private fun initSelectNameBar(nameList: ArrayList<NameEntity>) {
        mSelectBarAdapter = SelectBarAdapter(this, nameList)
        mSelectBarAdapter!!.setOnSelectBarClickListener(this)
        mSelectBarLayoutManager = LinearLayoutManager(this)
        mSelectBarLayoutManager?.orientation = LinearLayoutManager.HORIZONTAL
        mSelectNameBar.layoutManager = mSelectBarLayoutManager
        mSelectNameBar.adapter = mSelectBarAdapter

        if(nameList.size > 0) {
            nameList[0].mIsChoose = true
            initShowRecyclerView(nameList[0].mNameContent)
        }
    }

    private fun initRecyclerView(videoEntities: ArrayList<VideoEntity>) {
        mVideoListAdapter = VideoRecyclerViewAdapter(this, videoEntities)
        mVideoListAdapter!!.setImageClickListener(object: OnImageClickListener{
            override fun showLeftMenu(videoEntity: VideoEntity) {
                openLeftMenu(videoEntity)
            }
        })

        mVideoListLayoutManager = GridLayoutManager(this, 2)
        mVideoRecyclerView.layoutManager = mVideoListLayoutManager
        mVideoRecyclerView.adapter = mVideoListAdapter

        // 优化RecyclerView流畅度
        mVideoRecyclerView.setHasFixedSize(true)
        mVideoRecyclerView.setItemViewCacheSize(22)

    }

    private fun initDetailRecyclerView() {
        val detailData = ArrayList<VideoEntity>()

        mDetailListAdapter = DetailRecyclerViewAdapter(this, detailData)
        mDetailListLayoutManager = LinearLayoutManager(this)
        mDetailListLayoutManager?.orientation = LinearLayoutManager.HORIZONTAL
        mDetailRecyclerView.layoutManager = mDetailListLayoutManager
        mDetailRecyclerView.adapter = mDetailListAdapter

        // 优化RecyclerView流畅度
        mDetailRecyclerView.setHasFixedSize(true)
        mDetailRecyclerView.setItemViewCacheSize(10)
    }

    private fun initWaitingDialog(){
        mWaitingDialogBuilder = AlertDialog.Builder(this, R.style.WaitingAlertDialog)
        mWaitingDialogBuilder!!.setCancelable(false)
        mWaitingDialogBuilder!!.setView(R.layout.dialog_waiting)
    }

    private fun initRefreshLayout(selectName: String) {
        //设置头部刷新的样式
        mRefreshLayout.setRefreshHeader(ClassicsHeader(this))
        //设置页脚刷新的样式
        mRefreshLayout.setRefreshFooter(ClassicsFooter(this))
        //设置头部刷新时间监听
        mRefreshLayout.setOnRefreshListener(null)
        mRefreshLayout.setOnRefreshListener {
            mIsInUpdate = true
            (mDescriptionPresenter as VideoDescriptionPresenter).updateServerData(
                selectName, false, it
            ) { refreshLayout -> refreshLayout.finishRefresh() }
        }
        //设置尾部刷新时间监听
        mRefreshLayout.setOnLoadMoreListener(null)
        mRefreshLayout.setOnLoadMoreListener {
            mIsInUpdate = true
            (mDescriptionPresenter as VideoDescriptionPresenter).updateServerData(
                selectName, true, it
            ) { refreshLayout -> refreshLayout.finishLoadMore() }
        }
    }

    suspend fun showSelectBar(nameList: ArrayList<NameEntity>)
    = withContext(Dispatchers.Main) {
        initSelectNameBar(nameList)
        closeWaitingDialog()
    }

    suspend fun showVideoInfoRecyclerView(videoEntities: ArrayList<VideoEntity>)
    = withContext(Dispatchers.Main) {
        initRecyclerView(videoEntities)
        closeWaitingDialog()
    }

    suspend fun updateVideoInfoRecyclerView(videoEntities: ArrayList<VideoEntity>, isDown: Boolean,
                                            refreshLayout: RefreshLayout,
                                            refreshOperation: (RefreshLayout) -> Unit)
    = withContext(Dispatchers.Main) {
        if(videoEntities.size > 0){
            mVideoListAdapter?.updateVideoDescription(videoEntities)
            if(isDown)
                mVideoListLayoutManager?.scrollToPosition(0)
            else
                mVideoListLayoutManager?.scrollToPosition(
                    mVideoListAdapter!!.itemCount.coerceAtMost(
                        ConfigParams.getDescriptionNum - 1)
                )
        }
        refreshOperation(refreshLayout)
        mIsInUpdate = false
    }

    suspend fun switchNameRecyclerView(videoEntities: ArrayList<VideoEntity>)
    = withContext(Dispatchers.Main) {
        mVideoListAdapter?.updateVideoDescription(videoEntities)
        closeWaitingDialog()
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

    @SuppressLint("Recycle")
    fun openLeftMenu(videoEntity: VideoEntity){
        mDetailListAdapter?.updateDetailData(
            (mDescriptionPresenter as VideoDescriptionPresenter).getDetailData(videoEntity)
        )

        val alphaAnimator = ObjectAnimator.ofFloat(mLeftMenuContent, "alpha", 0f, 1f)
        val translationXAnimator = ObjectAnimator.ofFloat(mLeftMenuContent, "translationX", -150f, 0f)
        val leftAnimatorSet = AnimatorSet()
        leftAnimatorSet.play(alphaAnimator)
            .with(translationXAnimator)

        leftAnimatorSet.addListener(
            object : Animator.AnimatorListener{
                override fun onAnimationStart(p0: Animator) {
                    mLeftMenu.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(p0: Animator) {
                    mLeftMenu.visibility = View.VISIBLE
                }

                override fun onAnimationCancel(p0: Animator) {
                    mLeftMenu.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(p0: Animator) {
                }

            }
        )

        leftAnimatorSet.duration = 500
        leftAnimatorSet.start()
    }
    @SuppressLint("Recycle")
    private fun closeLeftMenu(){
        val alphaAnimator = ObjectAnimator.ofFloat(mLeftMenuContent, "alpha", 1f, 0f)
        val translationXAnimator = ObjectAnimator.ofFloat(mLeftMenuContent, "translationX", 0f, -150f)
        val leftAnimatorSet = AnimatorSet()
        leftAnimatorSet.play(alphaAnimator)
            .with(translationXAnimator)

        leftAnimatorSet.addListener(
            object : Animator.AnimatorListener{
                override fun onAnimationStart(p0: Animator) {

                }

                override fun onAnimationEnd(p0: Animator) {
                    mLeftMenu.visibility = View.GONE
                }

                override fun onAnimationCancel(p0: Animator) {
                    mLeftMenu.visibility = View.GONE
                }

                override fun onAnimationRepeat(p0: Animator) {

                }

            }
        )

        leftAnimatorSet.duration = 500
        leftAnimatorSet.start()
    }

    private fun initShowRecyclerView(selectName: String){
        initRefreshLayout(selectName)
        showWaitingDialog()
        (mDescriptionPresenter as VideoDescriptionPresenter).getServerData(selectName, true)
    }

    private fun switchShowRecyclerView(selectName: String) {
        initRefreshLayout(selectName)
        showWaitingDialog()
        (mDescriptionPresenter as VideoDescriptionPresenter).getServerData(selectName, false)
    }

    override fun onSelectBarClick(selectName: String) {
        switchShowRecyclerView(selectName)
    }

    override fun canClickItem(): Boolean {
        return if(!mIsInUpdate) {
            true
        }else {
            Toast.makeText(this, "当前正在刷新数据", Toast.LENGTH_SHORT).show()
            false
        }
    }
}