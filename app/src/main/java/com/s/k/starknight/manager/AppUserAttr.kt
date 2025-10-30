package com.s.k.starknight.manager

import android.os.Bundle
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.reyun.solar.engine.OnAttributionListener
import com.reyun.solar.engine.OnInitializationCallback
import com.reyun.solar.engine.SolarEngineConfig
import com.reyun.solar.engine.SolarEngineManager
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.Constant
import com.s.k.starknight.sk
import io.nekohasekai.sagernet.database.DataStore
import kotlinx.coroutines.launch
import org.json.JSONObject

class AppUserAttr {

    private val solarAttr by lazy { SolarAttr() }
    private val gpStoreAttr by lazy { GpStoreAttr() }

    fun isVip(): Boolean {
        //TODO修改用户属性
        if (BuildConfig.DEBUG) {
            return true
        }
        return sk.remoteConfig.userType == UserType.VIP.type
    }

    fun initUserAttr() {
        gpStoreAttr.initStore()
    }

    fun initSolar(fbId: String) {
        solarAttr.initSolar(fbId)
    }

    inner class GpStoreAttr {

        fun initStore() {
            if (!sk.preferences.gpStoreRef.isNullOrEmpty()) return
            sk.event.log("sk_store_init")
            val client = InstallReferrerClient.newBuilder(sk).build()
            val listener = object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(p0: Int) {
                    checkStoreResult(getSoreRef(client))
                    closeClient(client)
                }

                override fun onInstallReferrerServiceDisconnected() {

                }
            }
            client.startConnection(listener)
        }

        private fun getSoreRef(client: InstallReferrerClient): String? {
            return try {
                client.installReferrer?.installReferrer
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


        private fun closeClient(client: InstallReferrerClient) {
            try {
                client.endConnection()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun checkStoreResult(ref: String?) {
            sk.preferences.gpStoreRef = ref
            if (isVip(ref)) {
                sk.preferences.firebaseUserKey = UserKey.USER_KEY1.key
                sk.event.setUserAttr()
                sk.event.log("sk_store_vp")
            } else {
                sk.event.setUserAttr()
                sk.event.log("sk_store_nm")
            }
            DataStore.isVip = sk.user.isVip()
            // 普通用户在用户判断结果初始化广告
            if (!sk.user.isVip()){
                sk.initAd()
            }

        }

        private fun isVip(ref: String?): Boolean {
            if (ref.isNullOrEmpty()) return false
            return ref.contains("fb", true) || ref.contains(
                "fb4a", true
            ) || ref.contains("facebook", true) || ref.contains(
                "instagram", true
            ) || ref.contains("ig4a", true)
        }

    }


    inner class SolarAttr {

        private var initFbId: String? = null
        private val initListener = OnInitializationCallback {
            checkInitResult(it)
        }
        private val attrListener = object : OnAttributionListener {
            override fun onAttributionSuccess(p0: JSONObject?) {
                checkAttrResult()
            }

            override fun onAttributionFail(p0: Int) {
                sk.event.log("sk_sol_attr_err", Bundle().apply {
                    putString("msg", p0.toString())
                })
            }

        }

        fun initSolar(fbId: String) {
            if (fbId.isEmpty()) return
            if (!sk.isInitSolar || fbId != initFbId) {
                sk.event.log("sk_sol_init")
                this.initFbId = fbId
                sk.solar.preInit(sk, getSolarKey())
                sk.solar.initialize(
                    sk, getSolarKey(), SolarEngineConfig.Builder().build().apply {
                        fbAppID = fbId
                        attributionListener = attrListener
                    }, initListener
                )
            }

        }

        private fun checkInitResult(code: Int) {
            if (code != 0) {
                sk.event.log("sk_sol_init_err", Bundle().apply {
                    putString("msg", code.toString())
                })
                return
            }
            sk.scope.launch {
                val valueList = sk.db.skAdValueDao().queryAll()
                valueList.forEach {
                    if (sk.adValue.uploadAdValueToSolar(it)) {
                        sk.db.skAdValueDao().delete(it)
                    }
                }
            }
        }

        private fun checkAttrResult() {
            val channelId = try {
                SolarEngineManager.getInstance().attribution?.optString("channel_id")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            if (channelId.isNullOrEmpty()) return
            sk.preferences.solarChannelId = channelId
            if (channelId != "-1") {
                sk.preferences.firebaseUserKey = UserKey.USER_KEY1.key
                sk.event.setUserAttr()
                sk.event.log("sk_sol_vp")
            } else {
                sk.event.setUserAttr()
                sk.event.log("sk_sol_nm")
            }
            DataStore.isVip = sk.user.isVip()
            // 普通用户在用户判断结果初始化广告
            if (!sk.user.isVip()){
                sk.initAd()
            }
        }


        private fun getSolarKey(): String {
            return Constant.SK_SOLAR_KEY
        }


    }

    enum class UserKey(val key: String) {
        USER_KEY1("sk_user_key1"), USER_KEY2("sk_user_key2"),
    }

    enum class UserType(val type: Int) {
        VIP(1), NORMAL(0), ALL(2)
    }
}