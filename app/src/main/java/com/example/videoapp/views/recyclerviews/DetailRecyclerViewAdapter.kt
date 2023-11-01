package com.example.videoapp.views.recyclerviews

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R
import com.example.videoapp.entities.VideoEntity
import com.example.videoapp.views.activities.VideoPlayerActivity

class DetailRecyclerViewAdapter(context: Context, videoEntities: ArrayList<VideoEntity>): RecyclerView.Adapter<DetailItemViewHolder>() {
    private var mDetailEntities: ArrayList<VideoEntity>
    private val mContext: Context

    init {
        mDetailEntities = videoEntities
        mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_list_item, parent, false)

        return DetailItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDetailEntities.size
    }

    override fun onBindViewHolder(holder: DetailItemViewHolder, position: Int) {
        holder.mDetailItemImage.setImageBitmap(mDetailEntities[position].mBitmap)

        holder.mDetailItemImage.setOnClickListener(null)
        holder.mDetailItemImage.setOnClickListener {
            VideoPlayerActivity.startVideoPlayerActivity(mContext, mDetailEntities[position].mVideoUrl)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDetailData(detailData: ArrayList<VideoEntity>) {
        mDetailEntities = detailData
        notifyDataSetChanged()
    }

}