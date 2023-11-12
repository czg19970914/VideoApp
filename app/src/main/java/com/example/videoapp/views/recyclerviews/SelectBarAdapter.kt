package com.example.videoapp.views.recyclerviews

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.R
import com.example.videoapp.entities.NameEntity
import com.example.videoapp.presenters.VideoDescriptionPresenter

class SelectBarAdapter(context: Context, nameList: ArrayList<NameEntity>):
    RecyclerView.Adapter<SelectBarViewHolder>() {
    companion object{
        const val TAG = "SelectBarAdapter"
    }

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
            holder.mBottomLine.setBackgroundColor(ContextCompat.getColor(mContext, R.color.purple_200))
        }else {
            holder.mNameItemText.setTextColor(ContextCompat.getColor(mContext, R.color.name_select_item_text))
            holder.mBottomLine.setBackgroundColor(ContextCompat.getColor(mContext, R.color.name_select_item))
        }
        holder.mNameItemText.setOnClickListener(null)
        holder.mNameItemText.setOnClickListener {
            try {
                if (mOnSelectBarClickListener!!.canClickItem()) {
                    for (nameEntity in mNameList) {
                        nameEntity.mIsChoose = false
                    }
                    mNameList[position].mIsChoose = true

                    mOnSelectBarClickListener?.onSelectBarClick(mNameList[position].mNameContent)
                    notifyDataSetChanged()
                }
            } catch (e: NullPointerException) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    fun setOnSelectBarClickListener(onSelectBarClickListener: OnSelectBarClickListener){
        mOnSelectBarClickListener = onSelectBarClickListener
    }

    // 给Activity的回调接口
    interface OnSelectBarClickListener {
        fun onSelectBarClick(selectName: String)

        fun canClickItem(): Boolean
    }
}