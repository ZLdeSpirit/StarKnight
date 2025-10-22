package com.s.k.starknight.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkDialogAdLoadingBinding

class AdLoadingDialog (context: Context, private val countDownloadTime: Long, private val dismissCallback: () -> Unit) : Dialog(context, R.style.SK_DialogTheme) {
    private lateinit var mBinding: SkDialogAdLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        mBinding = SkDialogAdLoadingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

    }


    override fun dismiss() {
        super.dismiss()
        dismissCallback.invoke()
    }

    fun closeDialog(){
        dismiss()
    }

}