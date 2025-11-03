package com.s.k.starknight.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.s.k.starknight.R
import com.s.k.starknight.entity.ServerEntity

class SkSelectServerAdapter(val mList: ArrayList<ServerEntity>) : RecyclerView.Adapter<SkSelectServerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.sk_select_server_item, parent, false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = mList.size

    fun setData(list: ArrayList<ServerEntity>){
        if (mList.isNotEmpty()){
            mList.clear()
        }
        mList.addAll(list)
        notifyDataSetChanged()
    }

    private var mSelectListener: ((ServerEntity) -> Unit)? = null

    fun initServerEntity(name: String, callback: (ServerEntity?) -> Unit){
        var serverEntity = mList.find { it.countryParseName == name }
        if (serverEntity != null){
            mList.forEach { it.isSelected = false }
            serverEntity.isSelected = true
            notifyDataSetChanged()
        }else{
            serverEntity = mList[0]
            mList.forEach { it.isSelected = false }
            serverEntity.isSelected = true
            notifyDataSetChanged()
        }
        callback.invoke(serverEntity)
    }

    fun setSelectListener(listener: (ServerEntity) -> Unit) {
        mSelectListener = listener
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mCountryFlagIv = itemView.findViewById<ImageView>(R.id.countryIv)
        private val mCountryNameTv = itemView.findViewById<TextView>(R.id.countryTv)
        private val mSignalLevelIv = itemView.findViewById<ImageView>(R.id.signalIv)
        private val mCheckIv = itemView.findViewById<ImageView>(R.id.checkIv)
        private val mLineView = itemView.findViewById<View>(R.id.itemLineView)
        fun bind(item: ServerEntity, position: Int) {
            mCountryFlagIv.setImageResource(item.countryFlag)
            mCountryNameTv.text = item.countryParseName
            mSignalLevelIv.setImageResource(
                when (item.signalLevel) {
                    0 -> R.drawable.sk_ic_signal_0
                    1 -> R.drawable.sk_ic_signal_1
                    2 -> R.drawable.sk_ic_signal_2
                    3 -> R.drawable.sk_ic_signal_3
                    4 -> R.drawable.sk_ic_signal_4
                    else -> R.drawable.sk_ic_signal_0
                }
            )
            mCheckIv.setImageResource(if (item.isSelected) R.drawable.sk_ic_selected else R.drawable.sk_ic_selected_no)

            if (position == mList.size - 1) {
                mLineView.isVisible = false
            }
            itemView.setOnClickListener {
                mList.forEach { it.isSelected = false }
                item.isSelected = true
                notifyDataSetChanged()
                mSelectListener?.invoke(item)
            }
        }

    }
}