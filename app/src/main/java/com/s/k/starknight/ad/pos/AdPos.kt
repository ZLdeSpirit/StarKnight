package com.s.k.starknight.ad.pos

import android.util.Base64
import android.util.Log
import com.google.android.gms.ads.nativead.NativeAd
import com.s.k.starknight.ad.display.DisplayAd
import com.s.k.starknight.ad.display.DisplayConfig
import com.s.k.starknight.ad.info.SkAd
import com.s.k.starknight.ad.loader.SkAdLoader
import com.s.k.starknight.manager.AppUserAttr
import com.s.k.starknight.sk
import org.json.JSONObject

class AdPos(val adPos: String, private val loader: SkAdLoader) {

    private val adPosConfig by lazy { AdPosConfig() }

    fun preRequestAd() {
        Log.d("AdManager", "preload: start preload pos: $adPos")
        if (!adPosConfig.isOpen) {
            Log.d("AdManager", "preload: pos: $adPos is close")
            return
        }
        loader.preRequestAd(adPos)
    }

    fun requestAd(callback: (AdPos) -> Unit) {
        Log.d("AdManager", "load: start load pos: $adPos")
        if (!adPosConfig.isOpen) {
            Log.d("AdManager", "load: pos: $adPos is close")
            callback.invoke(this)
            return
        }
        loader.requestAd(adPos) {
            callback.invoke(this)
        }
    }

    fun displayAd(config: DisplayConfig) {
        Log.d("AdManager", "show: start show pos: $adPos")
        if (!config.activity.isVisibleActivity) {
            Log.d("AdManager", "show: pos: $adPos activity is not visible")
            config.closeCallback?.invoke()
            return
        }
        val ad = getAd()
        if (ad == null) {
            Log.d("AdManager", "show: pos: $adPos ad is null")
            config.closeCallback?.invoke()
            return
        }
        DisplayAd(ad, config, adPos).display()
    }

    fun displayNativeAd(config: DisplayConfig) {
        Log.d("AdManager", "show: start show pos: $adPos")
        if (!config.activity.isVisibleActivity) {
            Log.d("AdManager", "show: pos: $adPos activity is not visible")
            config.closeCallback?.invoke()
            return
        }
        val ad = getAd()
        if (ad == null) {
            Log.d("AdManager", "show: pos: $adPos ad is null")
            config.closeCallback?.invoke()
            return
        }
        DisplayAd(ad, config, adPos).showNative(ad.ad as NativeAd)
    }

    fun getAd(): SkAd? {
        if (adPosConfig.isOpen) return loader.getAd()
        return null
    }

    fun resetData() {
        adPosConfig.resetData()
    }

    inner class AdPosConfig {
        private var state = -1
        private var isGetStateSuccess = false

        val isOpen: Boolean
            get() {
                val currentState = getAdPosState()
                return when (currentState) {
                    AppUserAttr.UserType.VIP.type -> sk.user.isVip()
                    AppUserAttr.UserType.NORMAL.type -> !sk.user.isVip()
                    AppUserAttr.UserType.ALL.type -> true
                    else -> false
                }
            }

        private fun getAdPosState(): Int {
            if (!isGetStateSuccess) {
                val config = sk.remoteConfig.adPosConfig
                if (config.isEmpty()) return state
                try {
                    state =
                        JSONObject(String(Base64.decode(config, Base64.NO_WRAP))).optInt(adPos, -1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return state
        }

        fun resetData() {
            state = -1
            isGetStateSuccess = false
        }

    }

}