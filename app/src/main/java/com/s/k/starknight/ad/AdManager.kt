package com.s.k.starknight.ad

import com.s.k.starknight.ad.display.DisplayConfig
import com.s.k.starknight.ad.loader.SkAdLoader
import com.s.k.starknight.ad.pos.AdPos

class AdManager {

    val open = "sk_open"
    val languageNative = "sk_lang_nat"
    val languageInterstitial = "sk_lang_int"
    val createFinishInterstitial = "sk_create_fin_int"
    val createNative = "sk_create_nat"
    val returnInterstitial = "sk_return_int"
    val openInterstitial = "sk_open_int"
    val historyNative = "sk_history_nat"
    val resultNative = "sk_result_nat"
    val scanFinishInterstitial = "sk_scan_fin_int"
    val homeNative = "sk_home_nat"

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

    private fun getAdMold(adPos: String): AdMold {
        return when (adPos) {
            open -> AdMold.OPEN
            homeNative, createNative, resultNative, historyNative, languageNative -> AdMold.NATIVE
            else -> AdMold.INTERSTITIAL
        }
    }

    private fun getAdPos(adPos: String): AdPos {
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
            AdMold.OPEN.adMold -> AdMold.OPEN
            else -> null
        }
    }

    enum class AdMold(val adMold: String) {
        INTERSTITIAL("sk_interstitial"),
        NATIVE("sk_native"),
        OPEN("sk_open")
    }

}