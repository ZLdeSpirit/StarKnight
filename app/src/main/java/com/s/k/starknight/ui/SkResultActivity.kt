package com.s.k.starknight.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.s.k.starknight.R
import com.s.k.starknight.ad.display.NativeAdViewWrapper
import com.s.k.starknight.databinding.SkActivityResultBinding
import com.s.k.starknight.db.bean.SkRecommend
import com.s.k.starknight.dialog.AddTimeDialog
import com.s.k.starknight.dialog.RewardRetryDialog
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils

class SkResultActivity : BaseActivity() {
    companion object{
        const val KEY_CONNECT_FAIL = "key_connect_fail"
    }
    private val TAG = "SkResultActivity"
    private val mBinding by lazy { SkActivityResultBinding.inflate(layoutInflater) }
    private val addTimeDialog by lazy { AddTimeDialog(this) }

    override fun isDisplayReturnAd(): Boolean {
        return true
    }

    override fun onDisplayNativeInfo(): Pair<String, NativeAdViewWrapper> {
        return sk.ad.resultNative to mBinding.nativeAdWrapper
    }

    override fun needShowNative(): Boolean {
        if (sk.user.isVip()){
            return Utils.isConnectedState()
        }else{
            return !Utils.isConnectedState()
        }
    }

    override fun onRootView(): View {
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.apply {
            backIv.setOnClickListener {
                onReturnActivity()
            }

            adTimeLl.setOnClickListener {
                requestRewardAd()
            }
        }
        viewInit()
    }

    private fun viewInit(){
        val isConnectFail = intent.getBooleanExtra(KEY_CONNECT_FAIL, false)
        setLinkState(isConnectFail)
        mBinding.apply {
            adTimeTv.text = "+" + (sk.remoteConfig.remainTime / 60) + " mins"
            recyclerView.layoutManager = LinearLayoutManager(this@SkResultActivity, RecyclerView.HORIZONTAL, false)
            recyclerView.adapter = RecommendedAdapter()
        }
        handleAddTimeBtn()
    }

    private fun handleAddTimeBtn(){
        if (sk.user.isVip()){
            mBinding.adTimeLl.isVisible = Utils.isConnectedState()
        }else{
            mBinding.adTimeLl.isVisible = !Utils.isConnectedState()
        }
    }

    private fun setLinkState(isConnectFailed: Boolean){
        if (isConnectFailed){
            mBinding.apply {
                linkSuccessOrFailLl.isVisible = true
                disconnectedLl.isVisible = false
                setLinkSuccessOrFail(false)
            }
        }else{
            if (Utils.isConnectedState()){
                mBinding.apply {
                    linkSuccessOrFailLl.isVisible = true
                    disconnectedLl.isVisible = false
                    setLinkSuccessOrFail(true)
                }
            }else{
                mBinding.apply {
                    linkSuccessOrFailLl.isVisible = false
                    disconnectedLl.isVisible = true
                }
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

    private fun requestRewardAd() {
        ad.requestLoadingCheckRewardCacheAd(sk.ad.addTimeReward, {
            if (it) {
                RewardRetryDialog(this, {
                    requestRewardAd()
                }).show()
            }
        }, {
            sk.countDown.addRemainTime()
            addTimeDialog.show()
        })
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