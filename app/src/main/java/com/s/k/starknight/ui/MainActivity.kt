package com.s.k.starknight.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SpanUtils
import com.s.k.starknight.R
import com.s.k.starknight.databinding.SkActivityMainBinding
import com.s.k.starknight.dialog.AddTimeDialog
import com.s.k.starknight.tools.State

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: SkActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = SkActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initListener()

        connectState(State.CONNECTING)
    }

    private fun initListener(){
        mBinding.apply {
            selectLl.setOnClickListener {
                startActivity(Intent(this@MainActivity, SkSelectServerActivity::class.java))
            }
            mainCateIv.setOnClickListener {
                startActivity(Intent(this@MainActivity, SkApplicationActivity::class.java))
            }
            connectingLav.setOnClickListener {
                startActivity(Intent(this@MainActivity, SkResultActivity::class.java))
            }
            mainSettingsIv.setOnClickListener {
                startActivity(Intent(this@MainActivity, SkSettingsActivity::class.java))
            }
        }
        mBinding.adTimeLl.setOnClickListener {
            // 激励广告
                AddTimeDialog(this@MainActivity).show()
        }
    }

    private fun connectState(state: State) {
        when (state) {
            State.DISCONNECTED -> {
                mBinding.stateDotView.setBackgroundResource(R.drawable.sk_dot_disconnected_state)
                SpanUtils.with(mBinding.stateTv)
                    .append(getString(R.string.sk_status))
                    .append(getString(R.string.sk_disconnected))
                    .setForegroundColor(getColor(R.color.sk_disconnected_state))
                    .create()
                mBinding.connectingLav.isVisible = false
                mBinding.noConnectingLl.isVisible = true
                mBinding.noConnectIv.setImageResource(R.drawable.sk_ic_disconnected)
                mBinding.downloadSpeedTv.text = "--Mbps"
                mBinding.uploadSpeedTv.text = "--Mbps"
            }
            State.CONNECTING -> {
                mBinding.stateDotView.setBackgroundResource(R.drawable.sk_dot_connecting_state)
                SpanUtils.with(mBinding.stateTv)
                    .append(getString(R.string.sk_status))
                    .append(getString(R.string.sk_connecting))
                    .setForegroundColor(getColor(R.color.sk_connecting_state))
                    .create()
                mBinding.connectingLav.isVisible = true
                mBinding.noConnectingLl.isVisible = false
            }
            State.CONNECTED -> {
                mBinding.stateDotView.setBackgroundResource(R.drawable.sk_dot_connected_state)
                SpanUtils.with(mBinding.stateTv)
                    .append(getString(R.string.sk_status))
                    .append(getString(R.string.sk_connected))
                    .setForegroundColor(getColor(R.color.sk_connected_state))
                    .create()
                mBinding.connectingLav.isVisible = false
                mBinding.noConnectingLl.isVisible = true
                mBinding.noConnectIv.setImageResource(R.drawable.sk_ic_connected)
                mBinding.downloadSpeedTv.text = "32Mbps"
                mBinding.uploadSpeedTv.text = "21Mbps"
            }
        }
    }
}
