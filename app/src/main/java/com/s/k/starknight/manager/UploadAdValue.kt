package com.s.k.starknight.manager

import android.os.Bundle
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.firebase.analytics.FirebaseAnalytics
import com.reyun.solar.engine.SolarEngineManager
import com.reyun.solar.engine.infos.SEAdImpEventModel
import com.s.k.starknight.db.bean.SkAdValue
import com.s.k.starknight.sk
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency

class UploadAdValue {

    fun uploadShowAdValue(
        adValue: AdValue,
        adType: Int,
        adId: String,
        adPosition: String,
        responseInfo: ResponseInfo?
    ) {

        val value = adValue.valueMicros / 1000000.0
        sk.event.log("sk_ad_val_${adPosition}", Bundle().apply {
            putString(FirebaseAnalytics.Param.CURRENCY, adValue.currencyCode)
            putDouble(FirebaseAnalytics.Param.VALUE, value)
        })
        val totalValue = sk.preferences.getAdValue(4).first + value
        if (totalValue >= 0.01) {
            sk.preferences.setAdValue(4, 0.0, null, false)
            sk.event.log("sk_ad_val1", Bundle().apply {
                putString(FirebaseAnalytics.Param.CURRENCY, adValue.currencyCode)
                putDouble(FirebaseAnalytics.Param.VALUE, totalValue)
            })
        } else {
            sk.preferences.setAdValue(4, totalValue, adValue.currencyCode, false)
        }
        uploadShowAdValueToFacebook(adValue)
        val skAdValue = SkAdValue(
            adValue.currencyCode,
            adValue.valueMicros,
            adId,
            adType,
            responseInfo?.loadedAdapterResponseInfo?.adSourceName
        )
        if (!uploadAdValueToSolar(skAdValue)) {
            sk.scope.launch {
                sk.db.skAdValueDao().insert(skAdValue)
            }
        }
    }

    fun uploadClickAdValueToFacebook(adValue: AdValue?) {
        if (adValue == null) return
        val value = adValue.valueMicros / 1000000.0
        if (uploadClickAdValueToFacebook(value, adValue.currencyCode)) return
        sk.preferences.setAdValue(2, value, adValue.currencyCode)
    }

    private fun uploadShowAdValueToFacebook(adValue: AdValue) {
        val value = adValue.valueMicros / 1000000.0
        if (!uploadShowAdValueToFacebook(value, adValue.currencyCode)) {
            sk.preferences.setAdValue(1, value, adValue.currencyCode)
        }
        if (sk.preferences.isUploadedFbValue) return
        val totalValue = sk.preferences.getAdValue(3).first + value
        sk.preferences.setAdValue(3, totalValue, adValue.currencyCode, false)
        val limitValue = sk.remoteConfig.facebookValueLimit
        if (limitValue <= 0) return
        if (totalValue < limitValue) return
        if (!uploadMulAdValueToFacebook(totalValue, adValue.currencyCode)) return
        sk.preferences.isUploadedFbValue = true
        sk.preferences.setAdValue(3, 0.0, null, false)
    }

    fun uploadSaveShowAdValueToFacebook() {
        val (value, code) = sk.preferences.getAdValue(1)
        if (value <= 0 || code.isNullOrEmpty()) return
        if (!uploadShowAdValueToFacebook(value, code)) return
        sk.preferences.setAdValue(1, 0.0, null, false)
    }

    fun uploadSaveClickAdValueToFacebook() {
        val (value, code) = sk.preferences.getAdValue(2)
        if (value <= 0 || code.isNullOrEmpty()) return
        if (!uploadClickAdValueToFacebook(value, code)) return
        sk.preferences.setAdValue(2, 0.0, null, false)
    }


    private fun uploadMulAdValueToFacebook(value: Double, code: String): Boolean {
        if (!FacebookSdk.isInitialized()) return false
        val logger = AppEventsLogger.newLogger(sk)
        val parameters = Bundle()
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, code)
        logger.logEvent("sk_ad_val_fb_mul", value, parameters)
        return true
    }

    private fun uploadClickAdValueToFacebook(value: Double, code: String): Boolean {
        if (!FacebookSdk.isInitialized()) return false
        val logger = AppEventsLogger.newLogger(sk)
        logger.logEvent(AppEventsConstants.EVENT_NAME_AD_CLICK, value, Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_CURRENCY, code)
        })
        return true
    }

    private fun uploadShowAdValueToFacebook(value: Double, code: String): Boolean {
        if (!FacebookSdk.isInitialized()) return false
        val newVal = value * sk.remoteConfig.facebookValueMul
        val logger = AppEventsLogger.newLogger(sk)
        logger.logPurchase(BigDecimal.valueOf(newVal), Currency.getInstance(code))
        logger.logEvent(
            AppEventsConstants.EVENT_NAME_AD_IMPRESSION, newVal, Bundle().apply {
                putString(AppEventsConstants.EVENT_PARAM_CURRENCY, code)
            })
        return true
    }

    fun uploadAdValueToSolar(adValue: SkAdValue): Boolean {
        if (sk.isInitSolar) {
            try {
                SolarEngineManager.getInstance().trackAdImpression(SEAdImpEventModel().apply {
                    //变现平台名称
                    setAdNetworkPlatform(adValue.sourceName)
                    //聚合平台标识,admob SDK 设置成 "admob"
                    setMediationPlatform("admob")
                    //展示广告的类型，实际接入的广告类型,以此例激励视频为例adType = 1
                    setAdType(adValue.adType)
                    //变现平台的变现广告位ID
                    setAdNetworkADID(adValue.adId)
                    //广告ECPM
                    setEcpm(adValue.value / 1000.0)
                    //变现平台货币类型
                    setCurrencyType(adValue.unit)
                    //填充成功填TRUE即可
                    setRenderSuccess(true)
                })
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
}