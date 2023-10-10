package com.example.videoapp.recyclerviews

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R

class VideoItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val mVideoItemImage: ImageView
    val mVideoItemText: TextView

    init {
        mVideoItemImage = itemView.findViewById(R.id.video_item_image)
        mVideoItemText = itemView.findViewById(R.id.video_item_text)
    }
}