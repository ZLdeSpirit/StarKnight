package com.s.k.starknight.config

import kotlin.text.ifEmpty
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.s.k.starknight.manager.AppUserAttr
import com.s.k.starknight.sk

class RemoteConfig {

    private val config by lazy { Firebase.remoteConfig }

    fun fetchAndActivate() {
        setDefaultConfig()
        config.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                resetData()
            }

            override fun onError(error: FirebaseRemoteConfigException) {
            }
        })
        config.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                resetData()
            }
        }
    }

    private fun resetData() {
        sk.initFacebook()
        sk.ad.resetData()
    }

    private fun setDefaultConfig() {
        config.setDefaultsAsync(hashMapOf<String, Any>().apply {
            //TODO修改默认配置
            put(AppUserAttr.UserKey.USER_KEY1.key, AppUserAttr.UserType.VIP.type)
            put(AppUserAttr.UserKey.USER_KEY2.key, AppUserAttr.UserType.NORMAL.type)
            put("sk_ad_val_fb_mul", 1.0)
            put("sk_user_attr_limit_time", 48 * 60 * 60)
        })
    }

    val userType: Int
        get() {
            return config.getLong(
                sk.preferences.firebaseUserKey.orEmpty()
                    .ifEmpty { AppUserAttr.UserKey.USER_KEY2.key }).toInt()
        }

    val facebookValueMul: Double
        get() {
            return config.getDouble("sk_ad_val_fb_mul")
        }

    val facebookValueLimit: Double
        get() {
            return config.getDouble("sk_ad_val_fb_limit")
        }

    val facebookInfoConfig: String
        get() {
            return config.getString("sk_fb_info_txt")
        }

    val userAttrChangeLimitTime: Long
        get() {
            return config.getLong("sk_user_attr_limit_time")
        }

    val adMoldConfig: String
        get() {
            //TODO 修改广告配置
            return config.getString("sk_ad_mold_txt").ifEmpty {
                "eyJxdWlfbmF0aXZlIjp7InF1aV9pZF9hcnJheSI6W3sicXVpX2lkIjoiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzIyNDc2OTYxMTAiLCJxdWlfZ3JhZGUiOjF9XSwicXVpX2NvdW50IjoyfSwicXVpX2ludGVyc3RpdGlhbCI6eyJxdWlfaWRfYXJyYXkiOlt7InF1aV9pZCI6ImNhLWFwcC1wdWItMzk0MDI1NjA5OTk0MjU0NC8xMDMzMTczNzEyIiwicXVpX2dyYWRlIjoxfV0sInF1aV9jb3VudCI6Mn0sInF1aV9vcGVuIjp7InF1aV9pZF9hcnJheSI6W3sicXVpX2lkIjoiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzkyNTczOTU5MjEiLCJxdWlfZ3JhZGUiOjIsInF1aV9tb2xkIjoicXVpX29wZW4ifSx7InF1aV9pZCI6ImNhLWFwcC1wdWItMzk0MDI1NjA5OTk0MjU0NC8xMDMzMTczNzEyIiwicXVpX2dyYWRlIjoxLCJxdWlfbW9sZCI6InF1aV9pbnRlcnN0aXRpYWwifV0sInF1aV9jb3VudCI6MX19"
            }
        }

    val adPosConfig: String
        get() {
            return config.getString("sk_ad_pos_txt").ifEmpty {
                "eyJxdWlfb3BlbiI6MiwicXVpX2xhbmdfbmF0IjoxLCJxdWlfbGFuZ19pbnQiOjEsInF1aV9jcmVhdGVfZmluX2ludCI6MiwicXVpX2NyZWF0ZV9uYXQiOjIsInF1aV9yZXR1cm5faW50IjoxLCJxdWlfb3Blbl9pbnQiOjEsInF1aV9oaXN0b3J5X25hdCI6MSwicXVpX3Jlc3VsdF9uYXQiOjIsInF1aV9zY2FuX2Zpbl9pbnQiOjIsInF1aV9ob21lX25hdCI6Mn0="
            }
        }

}