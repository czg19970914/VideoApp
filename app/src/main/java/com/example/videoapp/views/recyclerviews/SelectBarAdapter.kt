package com.example.videoapp.views.recyclerviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R

class SelectBarAdapter(nameList: ArrayList<String>): RecyclerView.Adapter<SelectBarViewHolder>() {
    private val mNameList: ArrayList<String>

    init {
        mNameList = nameList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectBarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.name_list_item, parent, false)

        return SelectBarViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNameList.size
    }

    override fun onBindViewHolder(holder: SelectBarViewHolder, position: Int) {
        holder.mNameItemText.text = mNameList[position]
    }
}