package com.example.videoapp.views.recyclerviews

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R

class SelectBarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val mNameItemText: TextView

    init {
        mNameItemText = itemView.findViewById(R.id.name_item_text)
    }
}