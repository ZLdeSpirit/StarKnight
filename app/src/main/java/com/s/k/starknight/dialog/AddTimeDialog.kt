package com.s.k.starknight.dialog

import android.app.Dialog
import android.os.Bundle
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkDialogAddTimeBinding
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import com.s.k.starknight.ui.BaseActivity

class AddTimeDialog (val mActivity: BaseActivity) : Dialog(mActivity, R.style.SK_DialogTheme) {

    private lateinit var mBinding: SkDialogAddTimeBinding

    private val countdownTimeListener: (Long) -> Unit = { remainTime ->
        val time = Utils.formatMillis(remainTime)
        mBinding.remainTimeTv.text = time
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        mBinding = SkDialogAddTimeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        onInitView()

        sk.countDown.registerCountDownTime(countdownTimeListener)
    }

    private fun onInitView(){
        mBinding.apply {
            adTimeTv.text = "+" + (sk.remoteConfig.remainTime / 60) + " mins"
            closeBtn.setOnClickListener {
                dismiss()
            }

            adBtn.setOnClickListener {
                requestRewardAd()
            }
        }
    }

    private fun requestRewardAd() {
        mActivity.ad.requestLoadingCheckRewardCacheAd(sk.ad.addTimeReward, {
            if (it) {
                RewardRetryDialog(mActivity, {
                    requestRewardAd()
                }).show()
            }
        }, {
            sk.countDown.addRemainTime()
        })
    }

    override fun show() {
        if (!isShowing) {
            super.show()
        }
    }

    override fun dismiss() {
        super.dismiss()
        sk.countDown.unregisterCountDownTime(countdownTimeListener)
    }

}