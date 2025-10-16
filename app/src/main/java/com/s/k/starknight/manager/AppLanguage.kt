package com.s.k.starknight.manager

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import com.s.k.starknight.sk
import java.util.Locale

class AppLanguage {

    private val languageChangeCallbacks = mutableListOf<OnLanguageChangeCallback>()

    val appLanguageList: List<Pair<String, String>>
        get() {
            return listOf(
                "en" to "English",
                "pt" to "Português",
                "fr" to "Français",
                "hi" to "हिंदी",
                "es" to "Español",
                "ru" to "Русский",
                "ja" to "日本語",
                "ko" to "한국어",
            )
        }

    fun setLanguageCode(code: String?) {
        if (code.isNullOrEmpty() || sk.preferences.quickLanguageCode == code) return
        sk.preferences.quickLanguageCode = code
        val locale = getCurrentLanguage()
        setContextLanguage(sk, locale)
        notifyLanguageChange()
    }

    private fun getCurrentLanguage(): Locale {
        var setLanCode = sk.preferences.quickLanguageCode
        if (setLanCode.isNullOrEmpty()) {
            setLanCode = getLanguageCode()
            if (setLanCode.isNullOrEmpty()) return Locale.ENGLISH
            sk.preferences.quickLanguageCode = setLanCode
        }
        val (language, country) = if (setLanCode.contains("_")) {
            setLanCode.split("_".toRegex(), 2).map { it.trim() }
        } else {
            listOf(setLanCode, "")
        }
        return Locale(language, country.uppercase(Locale.US))
    }

    private fun getLanguageCode(): String? {
        // 加载程序支持的语言
        val languageInfoList = appLanguageList
        val phoneLans = mutableListOf<Locale>()
        val config = Resources.getSystem().configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for (i in 0 until config.locales.size()) {
                phoneLans.add(config.locales[i])
            }
        } else {
            phoneLans.add(config.locale)
        }
        for (sysLocale in phoneLans) {
            for (appLocale in languageInfoList) {
                if (appLocale.first == sysLocale.language.lowercase(Locale.US)) {
                    return appLocale.first
                }
            }
        }
        return null
    }

    fun setContextLanguage(context: Context?, locale: Locale? = null): Context? {
        if (context == null) return null
        val newLocale = locale ?: getCurrentLanguage()
        context.let {
            val res = it.resources
            val config = res.configuration
            config.setLocale(newLocale)
            val dm = res.displayMetrics
            val oldValue = DisplayMetrics()
            oldValue.setTo(dm)
            res.updateConfiguration(config, dm)
            res.displayMetrics.setTo(oldValue)
        }
        return context
    }

    fun addLanguageChangeCallback(callback: OnLanguageChangeCallback) {
        if (languageChangeCallbacks.contains(callback)) return
        languageChangeCallbacks.add(callback)
    }

    fun removeLanguageChangeCallback(callback: OnLanguageChangeCallback) {
        languageChangeCallbacks.remove(callback)
    }

    fun notifyLanguageChange() {
        languageChangeCallbacks.forEach { it.onLanguageChange() }
    }

    interface OnLanguageChangeCallback {
        fun onLanguageChange()
    }
}