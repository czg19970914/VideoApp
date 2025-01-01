package com.example.videoapp.views.recyclerviews

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R

class VideoItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val mVideoItemImage: ImageView = itemView.findViewById(R.id.video_item_image)
    val mVideoItemText: TextView = itemView.findViewById(R.id.video_item_text)
}