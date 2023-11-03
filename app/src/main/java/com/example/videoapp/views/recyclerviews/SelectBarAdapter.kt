package com.example.videoapp.views.recyclerviews

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R
import com.example.videoapp.entities.NameEntity

class SelectBarAdapter(context: Context, nameList: ArrayList<NameEntity>):
    RecyclerView.Adapter<SelectBarViewHolder>() {
    private val mNameList: ArrayList<NameEntity>
    private val mContext: Context

    private var mOnSelectBarClickListener: OnSelectBarClickListener? = null

    init {
        mNameList = nameList
        mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectBarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.name_list_item, parent, false)

        return SelectBarViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNameList.size
    }

    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: SelectBarViewHolder, position: Int) {
        holder.mNameItemText.text = mNameList[position].mNameContent
        if(mNameList[position].mIsChoose){
            holder.mNameItemText.setTextColor(ContextCompat.getColor(mContext, R.color.purple_200))
        }else {
            holder.mNameItemText.setTextColor(ContextCompat.getColor(mContext, R.color.black))
        }
        holder.mNameItemText.setOnClickListener(null)
        holder.mNameItemText.setOnClickListener {
            for(nameEntity in mNameList){
                nameEntity.mIsChoose = false
            }
            mNameList[position].mIsChoose = true

            mOnSelectBarClickListener?.onSelectBarClick(mNameList[position].mNameContent)

            notifyDataSetChanged()
        }
    }

    fun setOnSelectBarClickListener(onSelectBarClickListener: OnSelectBarClickListener){
        mOnSelectBarClickListener = onSelectBarClickListener
    }

    // 给Activity的回调接口
    interface OnSelectBarClickListener {
        fun onSelectBarClick(selectName: String)
    }
}