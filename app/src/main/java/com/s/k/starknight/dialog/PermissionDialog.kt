package com.s.k.starknight.dialog

import android.app.Dialog
import android.os.Bundle
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkDialogPermissionBinding
import com.s.k.starknight.sk
import com.s.k.starknight.ui.BaseActivity

class PermissionDialog(val mActivity: BaseActivity,private val callback: () -> Unit) : Dialog(mActivity, R.style.SK_DialogTheme) {

    private lateinit var mBinding: SkDialogPermissionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        mBinding = SkDialogPermissionBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        onInitView()

    }

    private fun onInitView(){
        mBinding.apply {
            closeBtn.setOnClickListener {
                dismiss()
            }

            openBtn.setOnClickListener {
                dismiss()
                callback.invoke()
            }
            sk.preferences.showOpenMsgTime = System.currentTimeMillis()
        }
    }

}