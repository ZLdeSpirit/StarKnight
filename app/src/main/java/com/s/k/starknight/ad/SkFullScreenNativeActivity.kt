package com.s.k.starknight.ad

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.s.k.starknight.databinding.SkActivityFullScreenNativeBinding
import com.s.k.starknight.sk

class SkFullScreenNativeActivity : AppCompatActivity(){
    companion object{
        var nativeAd: NativeAd? = null
        var mFullCallback: FullScreenContentCallback? = null
        var mAdPos: String = ""

        fun showNativeFull(activity: AppCompatActivity, nativeAd: NativeAd, adPos: String,fullCallback: FullScreenContentCallback? = null){
            this.nativeAd = nativeAd
            mAdPos = adPos
            this.mFullCallback = fullCallback
            activity.startActivity(Intent(activity, SkFullScreenNativeActivity::class.java))
        }
    }
    private lateinit var mBinding: SkActivityFullScreenNativeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = SkActivityFullScreenNativeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })

        if (nativeAd == null){
            mFullCallback?.onAdFailedToShowFullScreenContent(AdError(
                1000,
                "ad null",
                mAdPos,
            ))
            finish()
            return
        }

        showNative()

        mBinding.closeIv.setOnClickListener {
            finish()
        }

        handleCountDown()
    }

    private fun onBack(){
    }

    private fun showNative(){
        mFullCallback?.onAdShowedFullScreenContent()
        nativeAd?.let { nativeAd ->
            mBinding.apply {
                root.mediaView = mediaView
                root.callToActionView = actionBtn
                root.iconView = iconIv
                root.headlineView = titleTv
                root.bodyView = descTv
            }
            mBinding.root.run {
                nativeAd.icon?.let {
                    iconView?.isVisible = true
                    (iconView as ImageView).setImageDrawable(it.drawable)
                }
                nativeAd.mediaContent?.let {
                    mediaView?.mediaContent = it
                }
                (headlineView as TextView).apply {
                    this.isVisible = !nativeAd.headline.isNullOrEmpty()
                    this.text = nativeAd.headline
                }
                nativeAd.body?.let {
                    bodyView?.isVisible = it.isNotEmpty()
                    (bodyView as TextView).text = it
                }
                nativeAd.callToAction?.let {
                    callToActionView?.isVisible = it.isNotEmpty()
                    (callToActionView as TextView).text = it
                }
                setNativeAd(nativeAd)
            }

        }
    }


    private fun handleCountDown(){
        val countDownTime = sk.remoteConfig.getFullScreenNativeAdCountDown()
        if (countDownTime <= 0){
            mBinding.countDownBtn.isVisible = false
            mBinding.closeIv.isVisible = true
        }else{
            startCountDown(countDownTime)
        }

    }

    private fun startCountDown(countDownTime: Long){
        // 需要启动倒计时
        mBinding.countDownBtn.isVisible = true
        mBinding.closeIv.isVisible = false

        val countDownTimer = object : CountDownTimer(countDownTime * 1000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                mBinding.countDownBtn.text = ((millisUntilFinished / 1000) + 1).toString()
            }

            override fun onFinish() {
                mBinding.countDownBtn.isVisible = false
                mBinding.closeIv.isVisible = true
            }
        }
        countDownTimer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    private fun release(){
        nativeAd?.destroy()
        nativeAd = null
        mFullCallback?.onAdDismissedFullScreenContent()
    }
}