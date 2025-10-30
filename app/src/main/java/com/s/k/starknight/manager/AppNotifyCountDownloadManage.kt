package com.s.k.starknight.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.R
import com.s.k.starknight.StarKnight
import com.s.k.starknight.sk
import com.s.k.starknight.ui.SkSplashActivity
import io.nekohasekai.sagernet.database.DataStore
import kotlin.random.Random

object AppNotifyCountDownloadManage {
    private const val TAG = "AppNotifyCountDownloadManage"
    private const val NOTIFICATION_ID = 2
    private var timer: CountDownTimer? = null

    val flags =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

    val clickIntent = Intent(sk, SkSplashActivity::class.java).apply {
        putExtra(StarKnight.ExtraKey.OPEN_TYPE.key, StarKnight.ExtraValue.ADD_TIME_AND_CONNECT.value)// 加上,点击通知到应用后，自动连接vpn，并增加时间
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    private val customView =
        RemoteViews(sk.packageName, R.layout.sk_notification_layout).apply {
            val config = sk.preferences.getLastConfig()
            val title = config?.name ?: sk.getString(R.string.sk_app_name)
            setTextViewText(R.id.titleTv, title)
            setTextViewText(R.id.descTv, sk.getString(R.string.sk_connect_notify_tip))
            setImageViewBitmap(R.id.sk_image, BitmapFactory.decodeResource(sk.resources,R.mipmap.sk_ic_launcher))
//            setOnClickPendingIntent(R.id.sk_root, PendingIntent.getActivity(
//                sk,
//                0,
//                clickIntent,
//                flags
//            ))
        }

    fun checkForeground(isForeground: Boolean) {
        if (DataStore.checkIsVip()) {
            if (isForeground) {
                timer?.cancel()
            } else {
                handleBackground()
            }
        }
    }

    private fun handleBackground(){
        val intervalStartTime = sk.intervalStartTime
        val intervalEndTime = sk.intervalEndTime
        val remainTime = DataStore.remainTime
        val countDownTime = when {
            remainTime < intervalStartTime -> {
                intervalStartTime
            }
            remainTime in intervalStartTime .. intervalEndTime -> {
                remainTime
            }
            else -> {
                Random.nextInt(intervalStartTime.toInt(), intervalEndTime.toInt()).toLong()
            }
        }
        timer?.cancel()
        timer = object : CountDownTimer(countDownTime * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "millisUntilFinished:${millisUntilFinished}")
                }
            }

            override fun onFinish() {
                sendNotification(NOTIFICATION_ID,customView,null, false)
                if (DataStore.serviceState.canStop) {
                    StarKnight.Companion.stopService()
                }
            }
        }.apply {
            start()
        }
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
        builder.setContentIntent(PendingIntent.getActivity(sk, 0, clickIntent, flags))
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

}