package com.s.k.starknight.ad.display

import android.util.Log
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.ad.info.SkAd
import com.s.k.starknight.sk

class DisplayAd(
    private val ad: SkAd,
    private val config: DisplayConfig,
    private val adPos: String
) {

    private var adValue: AdValue? = null
    private val fullScreenCallback = object : FullScreenContentCallback() {
        override fun onAdClicked() {
            super.onAdClicked()
            ad.clickCallback?.invoke()
        }

        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
            if (BuildConfig.DEBUG) {
                Log.d("AdManager", "show: show success pos: $adPos")
            }
            displaySuccess()
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            super.onAdFailedToShowFullScreenContent(p0)
            if (BuildConfig.DEBUG) {
                Log.d("AdManager", "show: show error: ${p0.message} pos: $adPos")
            }
            config.closeCallback?.invoke()
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            if (BuildConfig.DEBUG) {
                Log.d("AdManager", "close: close ad pos: $adPos")
            }
            config.closeCallback?.invoke()
        }
    }

    fun display() {
        when (ad.ad) {
            is InterstitialAd -> displayInterstitialAd(ad.ad)
            is AppOpenAd -> displayAppOpenAd(ad.ad)
            is NativeAd -> showNative(ad.ad)
            else -> {
                if (BuildConfig.DEBUG) {
                    Log.d("AdManager", "display: ad type error pos: $adPos")
                }
                this.ad.isShow = false
                config.closeCallback?.invoke()
            }
        }
    }

    private fun displayInterstitialAd(ad: InterstitialAd) {
        this.ad.isShow = false
        this.ad.clickCallback = ::clickAd
        ad.setOnPaidEventListener {
            adValue = it
            sk.adValue.uploadShowAdValue(
                it,
                3,
                ad.adUnitId,
                adPos,
                ad.responseInfo
            )
        }
        ad.fullScreenContentCallback = fullScreenCallback
        ad.show(config.activity)
        callDisplay()
    }

    private fun displayAppOpenAd(ad: AppOpenAd) {
        this.ad.isShow = false
        this.ad.clickCallback = ::clickAd
        ad.setOnPaidEventListener {
            adValue = it
            sk.adValue.uploadShowAdValue(
                it,
                2,
                ad.adUnitId,
                adPos,
                ad.responseInfo
            )
        }
        ad.fullScreenContentCallback = fullScreenCallback
        ad.show(config.activity)
        callDisplay()
    }

    private fun showNative(ad: NativeAd) {
        val nativeAdView = config.nativeAdView
        if (nativeAdView == null) {
            if (BuildConfig.DEBUG) {
                Log.d("AdManager", "show: nativeView is null pos: $adPos")
            }
            config.closeCallback?.invoke()
            return
        }
        this.ad.isShow = false
        this.ad.clickCallback = ::clickAd
        nativeAdView.isVisible = true
        ad.setOnPaidEventListener {
            adValue = it
            sk.adValue.uploadShowAdValue(
                it,
                6,
                this.ad.adID.id,
                adPos,
                ad.responseInfo
            )
        }
        nativeAdView.displayAd(ad)
        callDisplay()
        displaySuccess()
    }

    private fun clickAd() {
        if (BuildConfig.DEBUG) {
            Log.d("AdManager", "click: click ad pos: $adPos")
        }
        sk.adValue.uploadClickAdValueToFacebook(adValue)
        sk.event.log("sk_click_ad_$adPos")
    }

    private fun displaySuccess() {
        sk.event.log("sk_disp_ad_$adPos")
    }

    private fun callDisplay() {
        sk.event.log("sk_call_disp_$adPos")
        if (BuildConfig.DEBUG) {
            Log.d("AdManager", "preload: start preload type: ${ad.loader.adMold.adMold}")
        }
        ad.loader.preRequestAd()
    }


}