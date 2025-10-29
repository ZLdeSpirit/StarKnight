package com.s.k.starknight.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkActivitySpeedTestResultBinding
import com.s.k.starknight.dialog.AddTimeDialog
import com.s.k.starknight.dialog.RewardRetryDialog
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.bg.SagerConnection
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.fmt.socks.SOCKSBean
import kotlin.random.Random

class SkSpeedTestResultActivity : BaseActivity(){
    private val TAG = "SkSettingsActivity"

    companion object{
        fun startActivity(activity: BaseActivity){
            activity.startActivity(Intent(activity, SkSpeedTestResultActivity::class.java).apply {
                val uploadSpeed = Random.nextFloat() * 0.9f + 0.3f
                val downloadSpeed = Random.nextFloat() * 0.9f + 0.3f
                putExtra("upload", uploadSpeed)
                putExtra("download", downloadSpeed)
            })
        }
    }

    private val mBinding by lazy { SkActivitySpeedTestResultBinding.inflate(layoutInflater) }

    override fun onCreatePreRequestPosList(): List<String>? {
        return arrayListOf(sk.ad.speedTestResultInterstitial)
    }

    override fun isDisplayReturnAd(): Boolean {
        return true
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

            selectLl.setOnClickListener {
                ad.requestLoadingCheckCacheAd(sk.ad.resultInterstitial) {
                    startActivityForResult(Intent(this@SkSpeedTestResultActivity, SkSelectServerActivity::class.java),100)
                }
            }

        }

        ad.requestLoadingHasCacheAd(sk.ad.speedTestResultInterstitial){
            val upload = intent.getFloatExtra("upload", 0.6f)
            val download = intent.getFloatExtra("download", 0.8f)

            mBinding.apply {
                centerSpeedTv.text = "%.2f".format(download)
                downloadSpeedTv.text = "${"%.2f".format(download)} Mbps"
                uploadSpeedTv.text = "${"%.2f".format(upload)} Mbps"
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
//            sk.countDown.addRemainTime()
            addTime()
            AddTimeDialog(this).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 200) {
            if (data != null) {
                val name = data.getStringExtra(SkSelectServerActivity.KEY_NAME) ?: ""
                val flag = data.getIntExtra(SkSelectServerActivity.KEY_FLAG, R.drawable.sk_ic_select_default)
                val config = data.getParcelableArrayListExtra<SOCKSBean>(SkSelectServerActivity.KEY_CONFIG)
                Log.i(TAG, "name: $name" + "\n" + "config:$config")

                mBinding.apply {
                    selectServerFlagIv.setImageResource(flag)
                    selectCountryTv.text = name
                }
            }
        }
    }
}