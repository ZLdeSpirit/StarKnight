package com.s.k.starknight.ui

import android.content.Intent
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
import com.s.k.starknight.dialog.RewardRetryDialog
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore

class SkResultActivity : BaseActivity(){
    companion object{
        const val KEY_CONNECT_FAIL = "key_connect_fail"
    }
    private val TAG = "SkResultActivity"
    private val mBinding by lazy { SkActivityResultBinding.inflate(layoutInflater) }

    override fun isDisplayReturnAd(): Boolean {
        return true
    }

    override fun onDisplayNativeInfo(): Pair<String, NativeAdViewWrapper> {
        return sk.ad.resultNative to mBinding.nativeAdWrapper
    }

    override fun onCreatePreRequestPosList(): List<String>? {
        return arrayListOf(sk.ad.resultInterstitial, sk.ad.speedTestResultInterstitial)
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

            speedTestBtn.setOnClickListener {
                ad.requestLoadingCheckCacheAd(sk.ad.resultInterstitial) {
                    SkSpeedTestResultActivity.startActivity(this@SkResultActivity)
                }
            }

            selectServerBtn.setOnClickListener {
                ad.requestLoadingCheckCacheAd(sk.ad.resultInterstitial) {
                    startActivity(Intent(this@SkResultActivity, SkSelectServerActivity::class.java))
                }
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
//        if (sk.user.isVip() && Utils.isConnectedState()){
//            AddTimeDialog(this).apply {
//                show()
//                setOnClickCloseListener {
//                    ad.requestLoadingCheckCacheAd(sk.ad.resultInterstitial) {
//                        this.dismiss()
//                    }
//                }
//            }
//        }

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
                extraFunLl.isVisible = sk.user.isVip()
            }else{
                linkSuccessOrFailIv.setImageResource(R.drawable.sk_ic_result_link_fail)
                linkSuccessOrFailTv.text = getString(R.string.sk_link_failure)
            }
        }
    }

    private fun requestRewardAd() {
        ad.requestLoadingCheckRewardCacheAd(sk.ad.addTimeReward, {
            when(it){
                0 -> {
                    RewardRetryDialog(this, {
                        requestRewardAd()
                    }).show()
                }
                1 -> {
                    addTime()
                }
                else -> {}
            }
        }, {
            addTime()
        })
    }

    private fun addTime(){
        if (DataStore.serviceState != BaseService.State.Connected){
            var remainTime = DataStore.remainTime
            remainTime = remainTime + sk.remoteConfig.remainTime
            DataStore.remainTime = remainTime
        }else{
            connection.service?.addTime(sk.remoteConfig.remainTime)
        }
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
    }

    override fun onServiceConnected(service: ISagerNetService) {
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