package com.s.k.starknight.manager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.messaging.FirebaseMessaging
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.Constant.TOKEN_UPLOAD_URL
import com.s.k.starknight.R
import com.s.k.starknight.sk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.math.abs

class NotifyManager {

    private var tokenJob: Job? = null
    private var requestCode = 49404
    private var notifyIdIndex = 0

    fun getRequestCode(): Int {
        return requestCode++
    }

    fun getNotifyIdIndex(): Int {
        return notifyIdIndex++
    }

    fun uploadToken(isForce: Boolean) {
        if (!isCanUploadToken(isForce)) return
        tokenJob = sk.scope.launch {
            uploadToken(6)
        }
    }

    private suspend fun uploadToken(count: Int) {
        if (count <= 0) return
        sk.event.log("sk_token_start")
        try {
            val deferred = CompletableDeferred<String?>()
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isSuccessful) {
                    deferred.complete(it.result)
                } else {
                    deferred.complete(null)
                }
            }
            val token = deferred.await()
            if (token.isNullOrEmpty() && !sk.preferences.canClack) {
                uploadTokenFail(count, "tok_null")
                return
            }
            startUploadToken(count, token)
        } catch (e: Exception) {
            e.printStackTrace()
            uploadTokenFail(count, e.message)
        }
    }

    private suspend fun startUploadToken(count: Int, token: String?) {
        val result = request(createUploadTokenRequest(token))
        if (!result.isSuccessful) {
            uploadTokenFail(count, result.code.toString())
            return
        }
        sk.preferences.canClack = false
        if (token.isNullOrEmpty()) {
            uploadTokenFail(count, "tok_null")
            return
        }
        sk.preferences.run {
            msgToken = token
            msgTokenTime = System.currentTimeMillis()
        }
        Log.i("NotifyManager", "upload token success")
        sk.event.log("sk_token_success")
    }

    private fun createUploadTokenRequest(token: String?): Request {
        return Request.Builder().url(TOKEN_UPLOAD_URL)
            .addHeader("LI", sk.packageName)
            .addHeader("PI", BuildConfig.VERSION_NAME)
            .post(uploadTokenParams(token).toRequestBody("application/json".toMediaType())).build()
    }

    private fun uploadTokenParams(token: String?): String {
        val json = JSONObject()
        val adId = try {
            AdvertisingIdClient.getAdvertisingIdInfo(sk).id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        json.put("corm", adId)
        if (!token.isNullOrEmpty()) {
            json.put("dag", token)
        }
        json.put("fug", sk.preferences.appInstallTime.toString())
        sk.preferences.gpStoreRef?.let {
            json.put("ref", it)
        }
        return json.toString()
    }

    private fun request(request: Request): Response {
        val builder = OkHttpClient.Builder()
        try {
            val trustManager = @SuppressLint("CustomX509TrustManager") object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?, authType: String?
                ) = Unit

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?, authType: String?
                ) = Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

            }
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf(trustManager), SecureRandom())
            builder.sslSocketFactory(sslContext.socketFactory, trustManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val client = builder.hostnameVerifier { _, _ -> true }.build()
        return client.newCall(request).execute()
    }

    private suspend fun uploadTokenFail(count: Int, msg: String?) {
        logUploadFail(msg)
        val newCount = count - 1
        if (newCount > 0) {
            delay(6000)
            uploadToken(newCount)
        }
    }

    private fun logUploadFail(msg: String?) {
        sk.event.log("sk_token_fail", Bundle().apply {
            putString("msg", msg)
        })
    }

    private fun isCanUploadToken(isForce: Boolean): Boolean {
        if (tokenJob?.isActive == true) return false
        if (isForce) return true
        return abs(System.currentTimeMillis() - sk.preferences.msgTokenTime) > DateUtils.DAY_IN_MILLIS
    }

    fun sendNotification(
        notifyId: Int,
        smallView: RemoteViews,
        bigViews: RemoteViews?,
        isDefault: Boolean
    ) {
        val channelId = "sk_channel_${notifyId}"
        NotificationManagerCompat.from(sk).run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    sk.getString(R.string.sk_app_name),
                    if (isDefault) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_HIGH
                )
                if (isDefault) {
                    channel.enableLights(false)
                    channel.enableVibration(false)
                    channel.setSound(null, null)
                } else {
                    channel.enableLights(true)
                    channel.enableVibration(true)
                    channel.vibrationPattern = longArrayOf(0, 1000)
                }
                createNotificationChannel(channel)
            }
            val notification =
                createNotification(notifyId, channelId, smallView, bigViews, isDefault)
            notify(notifyId, notification)
        }
    }

    private fun createNotification(
        notifyId: Int,
        channelId: String,
        smallView: RemoteViews,
        bigViews: RemoteViews?,
        isDefault: Boolean
    ): Notification {
        val builder = NotificationCompat.Builder(sk, channelId)
        builder.setSmallIcon(R.mipmap.sk_ic_launcher)
        if (isDefault) {
            builder.setVibrate(null)
            builder.setSound(null)
        } else {
            builder.setStyle(NotificationCompat.BigPictureStyle())
            builder.setVibrate(longArrayOf(0, 1000))
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }
        if (bigViews != null) {
            builder.setCustomBigContentView(bigViews)
            builder.setCustomHeadsUpContentView(bigViews)
        } else {
            builder.setCustomHeadsUpContentView(smallView)
            builder.setCustomBigContentView(smallView)
        }
        builder.setOngoing(false)
        builder.setGroup("sk_group_${notifyId}")
        builder.setGroupSummary(false)
        builder.setCustomContentView(smallView)
        builder.setContent(smallView)
        builder.setAutoCancel(true)
        return builder.build()
    }

    fun cleanNotification(notifyId: Int) {
        if (notifyId < 0) return
        try {
            NotificationManagerCompat.from(sk).cancel(notifyId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}