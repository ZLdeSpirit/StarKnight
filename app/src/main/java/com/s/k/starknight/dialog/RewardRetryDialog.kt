package com.s.k.starknight.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkDialogRewardRetryBinding

class RewardRetryDialog(context: Context,private val retryCallback: () -> Unit) : Dialog(context, R.style.SK_DialogTheme) {
    private lateinit var mBinding: SkDialogRewardRetryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        mBinding = SkDialogRewardRetryBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.noBtn.setOnClickListener {
            dismiss()
        }

        mBinding.retryBtn.setOnClickListener {
            dismiss()
            retryCallback.invoke()
        }
    }

}