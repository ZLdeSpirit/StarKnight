package com.s.k.starknight.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkDialogAddTimeBinding

class AddTimeDialog (context: Context) : Dialog(context, R.style.SK_DialogTheme) {

    private lateinit var mBinding: SkDialogAddTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        mBinding = SkDialogAddTimeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        onInitView()
    }

    private fun onInitView(){
        mBinding.apply {
            closeBtn.setOnClickListener {
                dismiss()
            }
        }
    }

}