package com.s.k.starknight.ad

import com.s.k.starknight.ad.display.DisplayConfig
import com.s.k.starknight.ad.loader.SkAdLoader
import com.s.k.starknight.ad.pos.AdPos

class AdManager {

    val open = "sk_open"
    val languageNative = "sk_lang_nat"
    val languageInterstitial = "sk_lang_int"
    val returnInterstitial = "sk_return_int"
    val resultNative = "sk_result_nat"
    val homeNative = "sk_home_nat"
    val homeInterstitial = "sk_home_int"
    val connectedInterstitial = "sk_connected_int"
    val disconnectSuccessInterstitial = "sk_disconnect_success_int"
    val settingsNative = "sk_settings_nat"
    val addTimeReward = "sk_add_time_reward"

    val resultInterstitial = "sk_result_int"

    val speedTestResultInterstitial = "sk_speed_test_result_int"

    private val adPosMap = hashMapOf<String, AdPos>()
    private val loaderMap = hashMapOf<AdMold, SkAdLoader>()

    fun preRequestAd(adPos: String) {
        val adPos = getAdPos(adPos)
        adPos.preRequestAd()
    }

    fun preRequestAd(adPosList: List<String>) {
        adPosList.forEach {
            preRequestAd(it)
        }
    }

    fun requestAd(adPos: String, callback: (AdPos) -> Unit) {
        val adPos = getAdPos(adPos)
        adPos.requestAd(callback)
    }

    fun displayAd(adPos: String, config: DisplayConfig) {
        val adPos = getAdPos(adPos)
        adPos.displayAd(config)
    }

    fun resetData() {
        adPosMap.values.forEach { it.resetData() }
        loaderMap.values.forEach { it.resetData() }
    }

    fun clearCacheAd(isClearConnectedAd: Boolean){
        loaderMap.values.forEach {
            it.clearCache(isClearConnectedAd)
        }
    }

    private fun getAdMold(adPos: String): AdMold {
        return when (adPos) {
            open -> AdMold.OPEN
            homeNative, resultNative, languageNative, settingsNative -> AdMold.NATIVE
            addTimeReward -> AdMold.REWARDEDINTERSTITIAL
            else -> AdMold.INTERSTITIAL
        }
    }

    fun getAdPos(adPos: String): AdPos {
        synchronized(adPosMap) {
            return adPosMap.getOrPut(adPos) { AdPos(adPos, getLoader(getAdMold(adPos))) }
        }
    }

    private fun getLoader(adMold: AdMold): SkAdLoader {
        synchronized(loaderMap) {
            return loaderMap.getOrPut(adMold) { SkAdLoader(adMold) }
        }
    }

    fun stringToAdMold(adMold: String): AdMold? {
        return when (adMold) {
            AdMold.INTERSTITIAL.adMold -> AdMold.INTERSTITIAL
            AdMold.NATIVE.adMold -> AdMold.NATIVE
            AdMold.NATIVEFULL.adMold -> AdMold.NATIVEFULL
            AdMold.OPEN.adMold -> AdMold.OPEN
            AdMold.REWARDEDINTERSTITIAL.adMold -> AdMold.REWARDEDINTERSTITIAL
            AdMold.REWARDVIDEO.adMold -> AdMold.REWARDVIDEO
            else -> null
        }
    }

    enum class AdMold(val adMold: String) {
        INTERSTITIAL("sk_interstitial"),
        NATIVE("sk_native"),
        OPEN("sk_open"),
        REWARDEDINTERSTITIAL("sk_rewarded_interstitial"),
        NATIVEFULL("sk_native_full"),
        REWARDVIDEO("sk_rewarded_video")
    }

}