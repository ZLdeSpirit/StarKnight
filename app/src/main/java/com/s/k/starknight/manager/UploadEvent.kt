package com.s.k.starknight.manager

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.sk

class UploadEvent {


    fun initUserUserProperty() {
        setInstallTime()
        setUserAttr()
    }

    private fun setInstallTime() {
        var day = (sk.preferences.appInstallTime - System.currentTimeMillis()) / DateUtils.DAY_IN_MILLIS
        if (day < 0) day = 0
        Firebase.analytics.setUserProperty("sk_day", day.toString())
        if (BuildConfig.DEBUG) {
            Log.d("AnalyticsManager", "sk_day: $day")
        }
    }

    fun setUserAttr() {
        val attr = if (sk.user.isVip()) {
            "vp"
        } else if (sk.preferences.gpStoreRef.isNullOrEmpty() && sk.preferences.solarChannelId.isNullOrEmpty()) {
            "un"
        } else {
            "nm"
        }
        Firebase.analytics.setUserProperty("sk_attr", attr)
        if (BuildConfig.DEBUG) {
            Log.d("AnalyticsManager", "sk_attr: $attr")
        }
    }


    fun log(event: String) {
        if (BuildConfig.DEBUG) {
            Log.d("AnalyticsManager", "event:$event, params:null")
        }
        Firebase.analytics.logEvent(event, null)
    }

    fun log(event: String, params: Bundle) {
        if (BuildConfig.DEBUG) {
            Log.d("AnalyticsManager", "event:$event, params:$params")
        }
        Firebase.analytics.logEvent(event, params)
    }

}