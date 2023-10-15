package com.example.videoapp.views.recyclerviews

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R
import com.example.videoapp.views.activities.VideoPlayerActivity
import com.example.videoapp.entities.VideoEntity

class VideoRecyclerViewAdapter(context: Context, videoEntities: ArrayList<VideoEntity>): RecyclerView.Adapter<VideoItemViewHolder>() {
    private var mVideoEntities: ArrayList<VideoEntity>
    private val mContext: Context

    init {
        mVideoEntities = videoEntities
        mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_list_item, parent, false)

        return VideoItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mVideoEntities.size
    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        holder.mVideoItemImage.setImageBitmap(mVideoEntities[position].mBitmap)
        holder.mVideoItemText.text = mVideoEntities[position].mVideoTitle

        holder.mVideoItemImage.setOnClickListener(null)
        holder.mVideoItemImage.setOnClickListener {
            VideoPlayerActivity.startVideoPlayerActivity(mContext, mVideoEntities[position].mVideoUrl)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateVideoDescription(videoEntities: ArrayList<VideoEntity>) {
        mVideoEntities = videoEntities
        notifyDataSetChanged()
    }
}