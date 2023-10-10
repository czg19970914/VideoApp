package com.example.videoapp.recyclerviews

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R
import com.example.videoapp.activities.VideoPlayerActivity
import com.example.videoapp.entities.VideoEntity

class VideoRecyclerViewAdapter(context: Context, videoEntities: List<VideoEntity>): RecyclerView.Adapter<VideoItemViewHolder>() {
    private val mVideoEntities: List<VideoEntity>
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
}