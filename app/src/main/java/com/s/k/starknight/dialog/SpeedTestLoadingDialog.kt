package com.s.k.starknight.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkDialogSpeedTestLoadingBinding
import com.s.k.starknight.ui.BaseActivity

class SpeedTestLoadingDialog(val mActivity: BaseActivity, private val countDownloadTime: Long, private val dismissCallback: () -> Unit) : Dialog
    (mActivity, R.style.SK_DialogTheme) {
    private lateinit var mBinding: SkDialogSpeedTestLoadingBinding

    private val timer = object : CountDownTimer(countDownloadTime, 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        mBinding = SkDialogSpeedTestLoadingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        timer.start()
    }


    fun closeDialog(){
        timer.cancel()
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        dismissCallback.invoke()
    }

    override fun show() {
        if (!mActivity.isDestroyed && !isShowing) {
            super.show()
        }
    }
}