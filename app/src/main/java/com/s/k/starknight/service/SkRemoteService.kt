package com.s.k.starknight.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.s.k.starknight.StarKnight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.Calendar
import com.s.k.starknight.sk
import com.s.k.starknight.R
import com.s.k.starknight.manager.AppUserAttr
import com.s.k.starknight.ui.SkSplashActivity
import io.nekohasekai.sagernet.database.DataStore

class SkRemoteService : FirebaseMessagingService() {

    private val msgIdList = listOf(84949, 90594, 89858, 78493, 28943)

    val hasNotifyPermission: Boolean
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return true
            }
            return ContextCompat.checkSelfPermission(
                sk, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("SkRemoteService", "onNewToken: $token")
        if (sk.preferences.msgToken != token) {
            sk.notify.uploadToken(true)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("SkRemoteService", "onMessageReceived: ${message.data}")
        sk.event.log("sk_rece_msg")

//        sendMessage(message.data)
    }

    fun sendMessage(data: Map<String, String>) {
        checkReceiveTime(data["ts"])
        if (checkSend(data)) {
            sendMessage(data["video_info"])
        }
    }

    private fun sendMessage(videoInfo: String?) {
        if (videoInfo.isNullOrEmpty()) {
            sk.event.log("sk_send_msg_null")
            return
        }
        try {
            val jsonArray = JSONArray(String(Base64.decode(videoInfo, Base64.NO_WRAP)))
            if (jsonArray.length() <= 0) return

            for (index in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(index)
                sendMessage(json.getString("or_url"), json.getString("cover"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sk.event.log("sk_send_msg_exce", Bundle().apply {
                putString("msg", e.message)
            })
        }
    }

    private fun sendMessage(parseUrl: String, coverUrl: String) {
        val notifyId = getNotifyId()
        val content = getMessageContent()
        sk.scope.launch {
            val bitmap = createBitmap(coverUrl)
            withContext(Dispatchers.Main) {
                val intent = createMessageIntent(notifyId, parseUrl)
                val viewSm = RemoteViews(sk.packageName, R.layout.sk_notification_layout)
                viewSm.setOnClickPendingIntent(R.id.sk_root, intent)
                viewSm.setTextViewText(R.id.titleTv, sk.getString(R.string.sk_app_name))
                viewSm.setTextViewText(R.id.descTv, sk.getString(content.first))
                viewSm.setImageViewBitmap(R.id.sk_image, bitmap)
                sk.notify.sendNotification(notifyId, viewSm, null, false)
                sk.event.log("sk_snd_msg")
            }
        }
    }

    private fun createBitmap(url: String): Bitmap? {
        try {
            val drawable =
                Glide.with(sk).asDrawable().load(url).submit().get() ?: return null
            val bitmap: Bitmap
            if (drawable is BitmapDrawable) {
                bitmap = drawable.bitmap
            } else {
                bitmap = androidx.core.graphics.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun createMessageIntent(notifyId: Int, parseUrl: String): PendingIntent {
        val intent = Intent(sk, SkSplashActivity::class.java).apply {
            putExtra(StarKnight.ExtraKey.OPEN_TYPE.key, 1)
            putExtra(StarKnight.ExtraKey.NOTIFY_ID.key, notifyId)
            `package` = sk.packageName
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        return PendingIntent.getActivity(
            sk,
            sk.notify.getRequestCode(),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    private fun getNotifyId(): Int {
        return msgIdList[sk.notify.getNotifyIdIndex() % msgIdList.size]
    }

    private fun getMessageContent(): Pair<Int, Int> {
        val contentList = msgContentList()
        val contentIndex = sk.preferences.notifyContentIndex % contentList.size
        sk.preferences.notifyContentIndex = contentIndex + 1
        return contentList[contentIndex]
    }

    private fun msgContentList(): List<Pair<Int, Int>> {
        return listOf(
            R.string.sk_notify_desc_1724 to R.string.sk_notify_act_1724,
            R.string.sk_notify_desc_1725 to R.string.sk_notify_act_1725,
            R.string.sk_notify_desc_1726 to R.string.sk_notify_act_1726,
            R.string.sk_notify_desc_1727 to R.string.sk_notify_act_1727,
            R.string.sk_notify_desc_1728 to R.string.sk_notify_act_1728,
            R.string.sk_notify_desc_1729 to R.string.sk_notify_act_1729,
            R.string.sk_notify_desc_1730 to R.string.sk_notify_act_1730,
            R.string.sk_notify_desc_1731 to R.string.sk_notify_act_1731,
            R.string.sk_notify_desc_1732 to R.string.sk_notify_act_1732,
            R.string.sk_notify_desc_1733 to R.string.sk_notify_act_1733,
            R.string.sk_notify_desc_1734 to R.string.sk_notify_act_1734,
            R.string.sk_notify_desc_1735 to R.string.sk_notify_act_1735,
            R.string.sk_notify_desc_1736 to R.string.sk_notify_act_1736,
            R.string.sk_notify_desc_1737 to R.string.sk_notify_act_1737,
            R.string.sk_notify_desc_1738 to R.string.sk_notify_act_1738,
            R.string.sk_notify_desc_1739 to R.string.sk_notify_act_1739,
            R.string.sk_notify_desc_1740 to R.string.sk_notify_act_1740,
            R.string.sk_notify_desc_1741 to R.string.sk_notify_act_1741,
            R.string.sk_notify_desc_1742 to R.string.sk_notify_act_1742,
            R.string.sk_notify_desc_1743 to R.string.sk_notify_act_1743,
            R.string.sk_notify_desc_1744 to R.string.sk_notify_act_1744,
            R.string.sk_notify_desc_1745 to R.string.sk_notify_act_1745,
            R.string.sk_notify_desc_1746 to R.string.sk_notify_act_1746,
            R.string.sk_notify_desc_1747 to R.string.sk_notify_act_1747,
            R.string.sk_notify_desc_1748 to R.string.sk_notify_act_1748,
            R.string.sk_notify_desc_1749 to R.string.sk_notify_act_1749,
            R.string.sk_notify_desc_1750 to R.string.sk_notify_act_1750,
            R.string.sk_notify_desc_1751 to R.string.sk_notify_act_1751,
            R.string.sk_notify_desc_1752 to R.string.sk_notify_act_1752,
            R.string.sk_notify_desc_1753 to R.string.sk_notify_act_1753,
            R.string.sk_notify_desc_1754 to R.string.sk_notify_act_1754,
            R.string.sk_notify_desc_1755 to R.string.sk_notify_act_1755,
            R.string.sk_notify_desc_1756 to R.string.sk_notify_act_1756,
            R.string.sk_notify_desc_1757 to R.string.sk_notify_act_1757,
            R.string.sk_notify_desc_1758 to R.string.sk_notify_act_1758,
            R.string.sk_notify_desc_1759 to R.string.sk_notify_act_1759,
            R.string.sk_notify_desc_1760 to R.string.sk_notify_act_1760,
            R.string.sk_notify_desc_1761 to R.string.sk_notify_act_1761,
            R.string.sk_notify_desc_1762 to R.string.sk_notify_act_1762,
            R.string.sk_notify_desc_1763 to R.string.sk_notify_act_1763,
            R.string.sk_notify_desc_1764 to R.string.sk_notify_act_1764,
            R.string.sk_notify_desc_1765 to R.string.sk_notify_act_1765,
            R.string.sk_notify_desc_1766 to R.string.sk_notify_act_1766,
            R.string.sk_notify_desc_1767 to R.string.sk_notify_act_1767,
            R.string.sk_notify_desc_1768 to R.string.sk_notify_act_1768,
            R.string.sk_notify_desc_1769 to R.string.sk_notify_act_1769,
            R.string.sk_notify_desc_1770 to R.string.sk_notify_act_1770,
            R.string.sk_notify_desc_1771 to R.string.sk_notify_act_1771,
            R.string.sk_notify_desc_1772 to R.string.sk_notify_act_1772,
            R.string.sk_notify_desc_1773 to R.string.sk_notify_act_1773
        )
    }

    private fun checkSend(data: Map<String, String>): Boolean {
        if (!hasNotifyPermission) {
            return false
        }
        sk.event.log("sk_rece_msg_per")
        if (!sk.user.isVip()) {
            return false
        }
        sk.event.log("sk_rece_msg_vip")
        if (sk.lifecycle.isAppVisible) {
            return false
        }
        sk.event.log("sk_rece_msg_app_in")
        val time = data["date_range"]
        var isTimeSend = true
        if (!time.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(time)
                if (jsonArray.length() >= 2) {
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val startTime = jsonArray.getInt(0)
                    val endTime = jsonArray.getInt(1)
                    isTimeSend = hour in startTime..endTime
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!isTimeSend) {
            return false
        }
        sk.event.log("sk_rece_msg_send")
        return true
    }

    private fun checkReceiveTime(time: String?) {
        if (time.isNullOrEmpty()) return
        try {
            val receiveTime = time.toLong()
            if (receiveTime < 0) return
            val firstReceiveTime = sk.preferences.receiveMsgTime
            if (firstReceiveTime <= 0) {
                sk.preferences.receiveMsgTime = receiveTime
                return
            }
            if (sk.user.isVip()) return
            if (receiveTime - firstReceiveTime < sk.remoteConfig.userAttrChangeLimitTime) return
            sk.preferences.firebaseUserKey = AppUserAttr.UserKey.USER_KEY1.key
            DataStore.isVip = sk.user.isVip()
            sk.event.setUserAttr()
            sk.event.log("sk_time_vp")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}