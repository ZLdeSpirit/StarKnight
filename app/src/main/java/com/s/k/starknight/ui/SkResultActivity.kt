package com.s.k.starknight.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkActivityResultBinding
import com.s.k.starknight.db.bean.SkRecommend

class SkResultActivity : AppCompatActivity() {
    private val TAG = "SkResultActivity"
    private lateinit var mBinding: SkActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = SkActivityResultBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.apply {
            backIv.setOnClickListener {
                finish()
            }
        }
        viewInit()
    }

    private fun viewInit(){
        setLinkState(false)
        mBinding.apply {
            recyclerView.layoutManager = LinearLayoutManager(this@SkResultActivity, RecyclerView.HORIZONTAL, false)
            recyclerView.adapter = RecommendedAdapter()
        }
    }

    private fun setLinkState(isLinkState: Boolean){
        if (isLinkState){
            mBinding.apply {
                linkSuccessOrFailLl.isVisible = true
                disconnectedLl.isVisible = false
                setLinkSuccessOrFail(false)
            }
        }else{
            mBinding.apply {
                linkSuccessOrFailLl.isVisible = false
                disconnectedLl.isVisible = true
            }
        }
    }

    private fun setLinkSuccessOrFail(isSuccess: Boolean){
        mBinding.apply {
            if (isSuccess){
                linkSuccessOrFailIv.setImageResource(R.drawable.sk_ic_result_link_succ)
                linkSuccessOrFailTv.text = getString(R.string.sk_link_successful)
            }else{
                linkSuccessOrFailIv.setImageResource(R.drawable.sk_ic_result_link_fail)
                linkSuccessOrFailTv.text = getString(R.string.sk_link_failure)
            }
        }
    }

    inner class RecommendedAdapter : RecyclerView.Adapter<RecommendedAdapter.ViewHolder>() {
        private val mList = mutableListOf<SkRecommend>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.sk_recommend_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position, mList[position])
        }

        override fun getItemCount(): Int {
            return mList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val mRecommendIv = itemView.findViewById<ImageView>(R.id.recommendIv)
            fun bind(position: Int, recommend: SkRecommend){
                Glide.with(itemView.context).load(recommend.path).into(mRecommendIv)

                mRecommendIv.setOnClickListener {

                }
            }
        }
    }
}