package com.s.k.starknight.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.s.k.starknight.R
import com.s.k.starknight.entity.SelectEntity

class SkSelectServerAdapter : RecyclerView.Adapter<SkSelectServerAdapter.ViewHolder>() {

    val mList = mutableListOf<SelectEntity>().apply {
        add(SelectEntity(R.drawable.sk_ic_flag_us, "United States", 1))
        add(SelectEntity(R.drawable.sk_ic_flag_fr, "France", 2))
        add(SelectEntity(R.drawable.sk_ic_flag_de, "Germany", 4))
        add(SelectEntity(R.drawable.sk_ic_flag_aus, "Australia", 2))
        add(SelectEntity(R.drawable.sk_ic_flag_in, "India", 0))
    }

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

    private var mSelectListener: ((SelectEntity) -> Unit)? = null

    fun setSelectListener(listener: (SelectEntity) -> Unit) {
        mSelectListener = listener
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mCountryFlagIv = itemView.findViewById<ImageView>(R.id.countryIv)
        private val mCountryNameTv = itemView.findViewById<TextView>(R.id.countryTv)
        private val mSignalLevelIv = itemView.findViewById<ImageView>(R.id.signalIv)
        private val mCheckIv = itemView.findViewById<ImageView>(R.id.checkIv)
        private val mLineView = itemView.findViewById<View>(R.id.itemLineView)
        fun bind(item: SelectEntity, position: Int) {
            mCountryFlagIv.setImageResource(item.countryFlag)
            mCountryNameTv.text = item.countryName
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