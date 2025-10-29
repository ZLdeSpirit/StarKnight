package com.s.k.starknight.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.s.k.starknight.R
import com.s.k.starknight.StarKnight
import com.s.k.starknight.databinding.SkDialogIpNotSupportBinding
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.database.DataStore

class IpNotSupportTipDialog(context: Context) : Dialog(context, R.style.SK_DialogTheme) {
    private lateinit var mBinding: SkDialogIpNotSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        mBinding = SkDialogIpNotSupportBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // 连接状态下，需要断开连接
        if (Utils.isConnectedState() && DataStore.serviceState.canStop){
            StarKnight.Companion.stopService()
        }
    }

}