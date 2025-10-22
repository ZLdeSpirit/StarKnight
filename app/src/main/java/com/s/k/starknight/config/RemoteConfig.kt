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

    private val serverListDefaultConfig = "ewogICAgInNob3dfbGlzdCI6IFsKICAgICAgICB7CiAgICAgICAgICAgICJuYW1lIjogIlVTLURhbGxhcy0xIiwKICAgICAgICAgICAgImNvZGUiOiAiVVMiLAogICAgICAgICAgICAiYWNjIjogWwogICAgICAgICAgICAgICAgImdhdGUua29va2VleS5pbmZvOjEwMDA6NTk1NjU5Ny03ZDVkZjdhYjozMTAzZTI5YS1VUy0zMDg2NTg2NC01bSIsCiAgICAgICAgICAgICAgICAiZ2F0ZS5rb29rZWV5LmluZm86MTAwMDo1OTU2NTk3LTdkNWRmN2FiOjMxMDNlMjlhLVVTLTg4MTQwMTUyLTVtIiwKICAgICAgICAgICAgICAgICJnYXRlLmtvb2tlZXkuaW5mbzoxMDAwOjU5NTY1OTctN2Q1ZGY3YWI6MzEwM2UyOWEtVVMtNDgyMTMwMDctNW0iLAogICAgICAgICAgICAgICAgImdhdGUua29va2VleS5pbmZvOjEwMDA6NTk1NjU5Ny03ZDVkZjdhYjozMTAzZTI5YS1VUy0xOTUwNDgwNC01bSIsCiAgICAgICAgICAgICAgICAiZ2F0ZS5rb29rZWV5LmluZm86MTAwMDo1OTU2NTk3LTdkNWRmN2FiOjMxMDNlMjlhLVVTLTkxNjU3MjU1LTVtIgogICAgICAgICAgICBdCiAgICAgICAgfSwKICAgICAgICB7CiAgICAgICAgICAgICJuYW1lIjogIlVTLURhbGxhcy0yIiwKICAgICAgICAgICAgImNvZGUiOiAiVVMiLAogICAgICAgICAgICAiYWNjIjogWwogICAgICAgICAgICAgICAgImdhdGUua29va2VleS5pbmZvOjEwMDA6NTk1NjU5Ny03ZDVkZjdhYjozMTAzZTI5YS1VUy05NDcxMzk0Mi01bSIsCiAgICAgICAgICAgICAgICAiZ2F0ZS5rb29rZWV5LmluZm86MTAwMDo1OTU2NTk3LTdkNWRmN2FiOjMxMDNlMjlhLVVTLTMyMjAwNjc4LTVtIiwKICAgICAgICAgICAgICAgICJnYXRlLmtvb2tlZXkuaW5mbzoxMDAwOjU5NTY1OTctN2Q1ZGY3YWI6MzEwM2UyOWEtVVMtMzY4MTEyMDQtNW0iLAogICAgICAgICAgICAgICAgImdhdGUua29va2VleS5pbmZvOjEwMDA6NTk1NjU5Ny03ZDVkZjdhYjozMTAzZTI5YS1VUy02MzY3Njk5Mi01bSIsCiAgICAgICAgICAgICAgICAiZ2F0ZS5rb29rZWV5LmluZm86MTAwMDo1OTU2NTk3LTdkNWRmN2FiOjMxMDNlMjlhLVVTLTcyNDM0NTI3LTVtIgogICAgICAgICAgICBdCiAgICAgICAgfSwKICAgICAgICB7CiAgICAgICAgICAgICJuYW1lIjogIlVTLURhbGxhcy0zIiwKICAgICAgICAgICAgImNvZGUiOiAiVVMiLAogICAgICAgICAgICAiYWNjIjogWwogICAgICAgICAgICAgICAgImdhdGUua29va2VleS5pbmZvOjEwMDA6NTk1NjU5Ny03ZDVkZjdhYjozMTAzZTI5YS1VUy03MTQ2NjU4NS01bSIsCiAgICAgICAgICAgICAgICAiZ2F0ZS5rb29rZWV5LmluZm86MTAwMDo1OTU2NTk3LTdkNWRmN2FiOjMxMDNlMjlhLVVTLTY2OTA5MTQyLTVtIiwKICAgICAgICAgICAgICAgICJnYXRlLmtvb2tlZXkuaW5mbzoxMDAwOjU5NTY1OTctN2Q1ZGY3YWI6MzEwM2UyOWEtVVMtNzM5MTI3NTUtNW0iLAogICAgICAgICAgICAgICAgImdhdGUua29va2VleS5pbmZvOjEwMDA6NTk1NjU5Ny03ZDVkZjdhYjozMTAzZTI5YS1VUy0xNzUzMzIxNC01bSIsCiAgICAgICAgICAgICAgICAiZ2F0ZS5rb29rZWV5LmluZm86MTAwMDo1OTU2NTk3LTdkNWRmN2FiOjMxMDNlMjlhLVVTLTYzNDkyMTY5LTVtIgogICAgICAgICAgICBdCiAgICAgICAgfQogICAgXQp9"

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
            put("sk_server_list_config", serverListDefaultConfig)
            put("sk_remain_time", 1200)
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
                "eyJza19uYXRpdmUiOnsic2tfaWRfYXJyYXkiOlt7InNrX2lkIjoiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzIyNDc2OTYxMTAiLCJza19ncmFkZSI6MX1dLCJza19jb3VudCI6Mn0sInNrX2ludGVyc3RpdGlhbCI6eyJza19pZF9hcnJheSI6W3sic2tfaWQiOiJjYS1hcHAtcHViLTM5NDAyNTYwOTk5NDI1NDQvMTAzMzE3MzcxMiIsInNrX2dyYWRlIjoxfV0sInNrX2NvdW50IjoyfSwic2tfb3BlbiI6eyJza19pZF9hcnJheSI6W3sic2tfaWQiOiJjYS1hcHAtcHViLTM5NDAyNTYwOTk5NDI1NDQvOTI1NzM5NTkyMSIsInNrX2dyYWRlIjoyLCJza19tb2xkIjoic2tfb3BlbiJ9LHsic2tfaWQiOiJjYS1hcHAtcHViLTM5NDAyNTYwOTk5NDI1NDQvMTAzMzE3MzcxMiIsInNrX2dyYWRlIjoxLCJza19tb2xkIjoic2tfaW50ZXJzdGl0aWFsIn1dLCJza19jb3VudCI6MX0sInNrX3Jld2FyZGVkX2ludGVyc3RpdGlhbCI6eyJza19pZF9hcnJheSI6W3sic2tfaWQiOiJjYS1hcHAtcHViLTM5NDAyNTYwOTk5NDI1NDQvNTM1NDA0NjM3OSIsInNrX2dyYWRlIjoxfV0sInNrX2NvdW50IjoyfX0="
            }
        }

    val adPosConfig: String
        get() {
            return config.getString("sk_ad_pos_txt").ifEmpty {
                "ewogICJza19vcGVuIjogMiwKICAic2tfbGFuZ19uYXQiOiAxLAogICJza19zZXR0aW5nc19uYXQiOiAxLAogICJza19yZXR1cm5faW50IjogMSwKICAic2tfYWRkX3RpbWVfcmV3YXJkIjogMiwKICAic2tfcmVzdWx0X25hdCI6IDIsCiAgInNrX2hvbWVfbmF0IjogMiwKICAic2tfaG9tZV9pbnQiOiAxLAogICJza19jb25uZWN0ZWRfaW50IjogMSwKICAic2tfZGlzY29ubmVjdF9zdWNjZXNzX2ludCI6ICIwIgp9"
            }
        }

    val serverConfig: String
        get() {
            return config.getString("sk_server_list_config").ifEmpty { serverListDefaultConfig }
        }

    val remainTime : Long
        get(){
            return config.getLong("sk_remain_time")
        }
}