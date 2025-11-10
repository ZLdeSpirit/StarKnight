package com.s.k.starknight.dialog

import android.app.Dialog
import android.os.Bundle
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkDialogAddTimeBinding
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import com.s.k.starknight.ui.BaseActivity
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.bg.SagerConnection
import io.nekohasekai.sagernet.database.DataStore

class AddTimeDialog (val mActivity: BaseActivity) : Dialog(mActivity, R
    .style.SK_DialogTheme), SagerConnection.Callback {

    private lateinit var mBinding: SkDialogAddTimeBinding
    var clickClose: (() -> Unit)? = null
    var closeNeedAddTime: Boolean = false

    val connection = SagerConnection(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND, true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        mBinding = SkDialogAddTimeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        onInitView()
    }

    fun setOnClickCloseListener(listener: (()-> Unit)){
        clickClose = listener
    }

    fun setCloseAddTime(needTime: Boolean){
        closeNeedAddTime = needTime
    }

    private fun onInitView(){
        mBinding.apply {
            adTimeTv.text = "+" + (sk.remoteConfig.remainTime / 60) + " mins"
            closeBtn.setOnClickListener {
                if (closeNeedAddTime){
                    addTime()
                }
                if (clickClose == null){
                    dismiss()
                }else{
                    clickClose?.invoke()
                    dismiss()
                }
            }

            adBtn.setOnClickListener {
                requestRewardAd()
            }
        }
    }

    private fun requestRewardAd() {
        mActivity.ad.requestLoadingCheckRewardCacheAd(sk.ad.addTimeReward, {
            when(it){
                0 -> {
                    RewardRetryDialog(mActivity, {
                        requestRewardAd()
                    }).show()
                }
                1 -> {
                    addTime()
                }
                else -> {}
            }
        }, {
            closeNeedAddTime = false
            //如果是激励插屏直接加时间，关闭时不会加
            addTime()
            dismiss()
        })
    }

    private fun addTime(){
        if (DataStore.serviceState != BaseService.State.Connected){
            var remainTime = DataStore.remainTime
            remainTime = remainTime + sk.remoteConfig.remainTime
            DataStore.remainTime = remainTime
            val time = Utils.formatMillis(remainTime * 1000)
            mBinding.remainTimeTv.text = time
        }else{
            connection.service?.addTime(sk.remoteConfig.remainTime)
        }
    }


    override fun show() {
        if (!isShowing) {
            super.show()
            val time = Utils.formatMillis(DataStore.remainTime * 1000)
            mBinding.remainTimeTv.text = time
            connection.connect(mActivity, this)
        }
    }

    override fun dismiss() {
        super.dismiss()
        connection.disconnect(mActivity)
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
    }

    override fun onServiceConnected(service: ISagerNetService) {
    }

    override fun countDown(time: Long, millis: Long) {
        val time = Utils.formatMillis(time * 1000)
        mBinding.remainTimeTv.text = time
    }

}