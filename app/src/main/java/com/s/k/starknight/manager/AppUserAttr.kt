package com.s.k.starknight.manager

import android.os.Bundle
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.reyun.solar.engine.OnAttributionListener
import com.reyun.solar.engine.OnInitializationCallback
import com.reyun.solar.engine.SolarEngineConfig
import com.reyun.solar.engine.SolarEngineManager
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.sk
import kotlinx.coroutines.launch
import org.json.JSONObject

class AppUserAttr {

//    private val solarAttr by lazy { SolarAttr() }
//    private val gpStoreAttr by lazy { GpStoreAttr() }

    fun isVip(): Boolean {
        //TODO修改用户属性
        if (BuildConfig.DEBUG) {
            return true
        }
//        return sk.remoteConfig.userType == UserType.VIP.type
        return true
    }

//    fun initUserAttr() {
//        gpStoreAttr.initStore()
//    }
//
//    fun initSolar(fbId: String) {
//        solarAttr.initSolar(fbId)
//    }

//    inner class GpStoreAttr {
//
//        fun initStore() {
//            if (!quick.preferences.gpStoreRef.isNullOrEmpty()) return
//            quick.event.log("qui_store_init")
//            val client = InstallReferrerClient.newBuilder(quick).build()
//            val listener = object : InstallReferrerStateListener {
//                override fun onInstallReferrerSetupFinished(p0: Int) {
//                    checkStoreResult(getSoreRef(client))
//                    closeClient(client)
//                }
//
//                override fun onInstallReferrerServiceDisconnected() {
//
//                }
//            }
//            client.startConnection(listener)
//        }
//
//        private fun getSoreRef(client: InstallReferrerClient): String? {
//            return try {
//                client.installReferrer?.installReferrer
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        }
//
//
//        private fun closeClient(client: InstallReferrerClient) {
//            try {
//                client.endConnection()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        private fun checkStoreResult(ref: String?) {
//            quick.preferences.gpStoreRef = ref
//            if (isVip(ref)) {
//                quick.preferences.firebaseUserKey = UserKey.USER_KEY1.key
//                quick.event.setUserAttr()
//                quick.event.log("qui_store_vp")
//            } else {
//                quick.event.setUserAttr()
//                quick.event.log("qui_store_nm")
//            }
//        }
//
//        private fun isVip(ref: String?): Boolean {
//            if (ref.isNullOrEmpty()) return false
//            return ref.contains("fb", true) || ref.contains(
//                "fb4a", true
//            ) || ref.contains("facebook", true) || ref.contains(
//                "instagram", true
//            ) || ref.contains("ig4a", true)
//        }
//
//    }
//
//
//    inner class SolarAttr {
//
//        private var initFbId: String? = null
//        private val initListener = OnInitializationCallback {
//            checkInitResult(it)
//        }
//        private val attrListener = object : OnAttributionListener {
//            override fun onAttributionSuccess(p0: JSONObject?) {
//                checkAttrResult()
//            }
//
//            override fun onAttributionFail(p0: Int) {
//                quick.event.log("qui_sol_attr_err", Bundle().apply {
//                    putString("msg", p0.toString())
//                })
//            }
//
//        }
//
//        fun initSolar(fbId: String) {
//            if (fbId.isEmpty()) return
//            if (!quick.isInitSolar || fbId != initFbId) {
//                quick.event.log("qui_sol_init")
//                this.initFbId = fbId
//                quick.solar.preInit(quick, getSolarKey())
//                quick.solar.initialize(
//                    quick, getSolarKey(), SolarEngineConfig.Builder().build().apply {
//                        fbAppID = fbId
//                        attributionListener = attrListener
//                    }, initListener
//                )
//            }
//
//        }
//
//        private fun checkInitResult(code: Int) {
//            if (code != 0) {
//                quick.event.log("qui_sol_init_err", Bundle().apply {
//                    putString("msg", code.toString())
//                })
//                return
//            }
//            quick.scope.launch {
//                val valueList = quick.db.quickAdValueDao().queryAll()
//                valueList.forEach {
//                    if (quick.adValue.uploadAdValueToSolar(it)) {
//                        quick.db.quickAdValueDao().delete(it)
//                    }
//                }
//            }
//        }
//
//        private fun checkAttrResult() {
//            val channelId = try {
//                SolarEngineManager.getInstance().attribution?.optString("channel_id")
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//            if (channelId.isNullOrEmpty()) return
//            quick.preferences.solarChannelId = channelId
//            if (channelId != "-1") {
//                quick.preferences.firebaseUserKey = UserKey.USER_KEY1.key
//                quick.event.setUserAttr()
//                quick.event.log("qui_sol_vp")
//            } else {
//                quick.event.setUserAttr()
//                quick.event.log("qui_sol_nm")
//            }
//        }
//
//
//        private fun getSolarKey(): String {
//            //TODO修改热云key
//            return "ri0ai-fa"
//        }
//
//
//    }
//
//    enum class UserKey(val key: String) {
//        USER_KEY1("qui_user_key1"), USER_KEY2("qui_user_key2"),
//    }
//
//    enum class UserType(val type: Int) {
//        VIP(1), NORMAL(0), ALL(2)
//    }
}