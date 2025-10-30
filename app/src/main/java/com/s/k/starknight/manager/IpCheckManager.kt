package com.s.k.starknight.manager

import android.content.res.Resources
import com.s.k.starknight.Constant
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * ip为CN的不可使用
 */
object IpCheckManager {
    private val TAG = "IpCheckManager"
    private var isAllowConnect: Boolean? = null

    fun getAllowState(): Boolean?{
        return isAllowConnect
    }

    /**
     * true:直接使用
     * false：不可使用，弹窗提示
     */
    fun checkIp(callback: (Boolean) -> Unit) {
        sk.scope.launch {
            var response = request(Constant.IP_CHECK_URL_2)
//            if (response == null){
//                // 判断设备是否是大陆设备
//                if (combineIsChina()){
//                    isAllowConnect = false
//                    callback(false)
//                }else{
//                    isAllowConnect = true
//                    callback.invoke(true)
//                }
//                return@launch
//            }
            if (response == null || !response.isSuccessful) {
                Utils.logDebugI(TAG, "first check ip response:${response?.message}")
                response = request(Constant.IP_CHECK_URL_1)
                if (response == null || !response.isSuccessful){
                    Utils.logDebugI(TAG, "second check ip response:${response?.message}")
                    checkDeviceIsChina(callback)
                    return@launch
                }
                // 请求成功
                handleResult(response, callback)
                return@launch
            }
            //  请求成功
            handleResult(response, callback)
        }
    }

    private suspend fun handleResult(response: Response, callback: (Boolean) -> Unit) {
        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                val body = response.body
                if (body == null) {
                    isAllowConnect = true
                    callback.invoke(true)
                    return@withContext
                }
                val result = body.string()
                if (result.contains("cn", true)){
                    Utils.logDebugI(TAG, "ip network check is china region")
                    isAllowConnect = false
                    callback(false)
                }else{
                    Utils.logDebugI(TAG, "ip network check is not china region")
                    isAllowConnect = true
                    callback(true)
                }
            } else {
                checkDeviceIsChina(callback)
            }
        }
    }

    private fun checkDeviceIsChina(callback: (Boolean) -> Unit){
        // 判断设备是否是大陆设备
        if (combineIsChina()){
            Utils.logDebugI(TAG, "device check is china region")
            isAllowConnect = false
            callback(false)
        }else{
            Utils.logDebugI(TAG, "device check is not china region")
            isAllowConnect = true
            callback.invoke(true)
        }
    }

    private fun combineIsChina(): Boolean{
        return isChinaRegion() || isDeviceInChina()
    }

    private fun isChinaRegion(): Boolean {
        val configuration = Resources.getSystem().configuration
        val localeList = configuration.locales
        for (i in 0 until localeList.size()) {
            val locale = localeList[i]
            if (locale.country.equals("cn", ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun isDeviceInChina(): Boolean {
        return Locale.getDefault() == Locale.CHINA ||
                Locale.getDefault() == Locale.CHINESE ||
                Locale.getDefault().country.equals("cn", ignoreCase = true)
    }


    private fun request(url: String): Response? {
        val builder = OkHttpClient.Builder()
        try {
            val request = Request.Builder().url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            val client = builder
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .hostnameVerifier { _, _ -> true }
                .build()
            val response = client.newCall(request).execute()
            return response
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}