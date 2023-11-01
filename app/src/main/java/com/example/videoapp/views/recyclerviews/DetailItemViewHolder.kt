package com.example.videoapp.views.recyclerviews

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R

class DetailItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val mDetailItemImage: ImageView

    init {
        mDetailItemImage = itemView.findViewById(R.id.detail_item_image)
    }
}