package com.example.videoapp.views.recyclerviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R
import com.example.videoapp.entities.VideoEntity

class DetailRecyclerViewAdapter(videoEntities: ArrayList<VideoEntity>): RecyclerView.Adapter<DetailItemViewHolder>() {
    private var mDetailEntities: ArrayList<VideoEntity>

    init {
        mDetailEntities = videoEntities
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
    }

}