package com.s.k.starknight.manager

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.s.k.starknight.entity.LastConfig
import com.s.k.starknight.sk

class AppPreferences {

    private val config by lazy {
        sk.getSharedPreferences(
            "sk_preferences",
            Context.MODE_PRIVATE
        )
    }

    val appInstallTime: Long
        get() {
            try {
                return sk.packageManager.getPackageInfo(
                    sk.packageName,
                    PackageManager.GET_ACTIVITIES
                ).firstInstallTime
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return quickOpenTime
        }

    var quickLanguageCode: String?
        get() {
            return config.getString("sk_lan_code", null)
        }
        set(value) {
            config.edit(true) { putString("sk_lan_code", value) }
        }

    var quickOpenTime: Long
        get() {
            return config.getLong("sk_op_time", 0)
        }
        set(value) {
            config.edit(true) { putLong("sk_op_time", value) }
        }

    var gpStoreRef: String?
        get() {
            return config.getString("sk_gp_store_ref", null)
        }
        set(value) {
            config.edit(true) { putString("sk_gp_store_ref", value) }
        }

    var solarChannelId: String?
        get() {
            return config.getString("sk_sol_channel_id", null)
        }
        set(value) {
            config.edit(true) { putString("sk_sol_channel_id", value) }
        }

    var firebaseUserKey: String?
        get() {
            return config.getString("sk_fire_user_key", null)
        }
        set(value) {
            config.edit(true) { putString("sk_fire_user_key", value) }
        }

    fun setAdValue(type: Int, value: Double, code: String?, isAdd: Boolean = true) {
        val saveValue = if (isAdd) {
            getDouble("sk_ad_val_$type") + value
        } else {
            value
        }
        config.edit(true) { putString("sk_ad_val_$type", saveValue.toString()) }
        config.edit(true) { putString("sk_ad_val_code_$type", code) }
    }

    fun getAdValue(type: Int): Pair<Double, String?> {
        return getDouble("sk_ad_val_$type") to config.getString("sk_ad_val_code_$type", null)
    }

    private fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return config.getString(key, defaultValue.toString())?.toDouble() ?: defaultValue
    }

    var isUploadedFbValue: Boolean
        get() {
            return config.getBoolean("sk_ad_val_fb_uploaded", false)
        }
        set(value) {
            config.edit(true) { putBoolean("sk_ad_val_fb_uploaded", value) }
        }

    var isSetAppLanguage: Boolean
        get() {
            return config.getBoolean("sk_set_app_lang", false)
        }
        set(value) {
            config.edit(true) { putBoolean("sk_set_app_lang", value) }
        }

    var msgToken: String?
        get() {
            return config.getString("sk_msg_token", null)
        }
        set(value) {
            config.edit(true) { putString("sk_msg_token", value) }
        }

    var msgTokenTime: Long
        get() {
            return config.getLong("sk_msg_token_time", 0)
        }
        set(value) {
            config.edit(true) { putLong("sk_msg_token_time", value) }
        }

    var canClack: Boolean
        get() {
            return config.getBoolean("sk_can_clack", true)
        }
        set(value) {
            config.edit(true) { putBoolean("sk_can_clack", value) }
        }

    var notifyContentIndex: Int
        get() {
            return config.getInt("sk_noti_content_index", 0)
        }
        set(value) {
            config.edit(true) { putInt("sk_noti_content_index", value) }
        }

    var receiveMsgTime: Long
        get() {
            return config.getLong("sk_rece_msg_time", 0)
        }
        set(value) {
            config.edit(true) { putLong("sk_rece_msg_time", value) }
        }

    var showOpenMsgTime: Long
        get() {
            return config.getLong("sk_show_open_msg_time", 0)
        }
        set(value) {
            config.edit(true) { putLong("sk_show_open_msg_time", value) }
        }

    fun setLastConfig(lastConfig: LastConfig){
        val gson = Gson()
        val json = gson.toJson(lastConfig)
        config.edit(true) { putString("sk_last_config", json) }
    }

    fun getLastConfig(): LastConfig? {
        return try {
            val jsonString = config.getString("sk_last_config", null)
            if (jsonString.isNullOrEmpty()) {
                null
            } else {
                val gson = Gson()
                gson.fromJson(jsonString, LastConfig::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    var remainTime: Long
        get() {
            //todo
            return config.getLong("sk_remain_time", 180)
//            return config.getLong("sk_remain_time", 40)
        }
        set(value) {
            config.edit(true) { putLong("sk_remain_time", value) }
        }

    var isFirstSplash: Boolean
        get() {
            return config.getBoolean("sk_first_splash", true)
        }
        set(value) {
            config.edit(true) { putBoolean("sk_first_splash", value) }
        }
}