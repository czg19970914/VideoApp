package com.example.videoapp.views.recyclerviews

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R

class SelectBarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val mNameItemText: TextView = itemView.findViewById(R.id.name_item_text)
    val mBottomLine: View = itemView.findViewById(R.id.name_item_bottom_line)
}