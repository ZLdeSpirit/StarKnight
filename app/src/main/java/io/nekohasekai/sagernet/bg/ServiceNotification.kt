package io.nekohasekai.sagernet.bg

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.s.k.starknight.R
import com.s.k.starknight.StarKnight
import com.s.k.starknight.sk
import com.s.k.starknight.ui.SkSplashActivity
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.ktx.runOnMainDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * User can customize visibility of notification since Android 8.
 * The default visibility:
 *
 * Android 8.x: always visible due to system limitations
 * VPN:         always invisible because of VPN notification/icon
 * Other:       always visible
 *
 * See also: https://github.com/aosp-mirror/platform_frameworks_base/commit/070d142993403cc2c42eca808ff3fafcee220ac4
 */
class ServiceNotification(
    private val service: BaseService.Interface, val title: String,
    channel: String, visible: Boolean = false,
) : BroadcastReceiver() {
    companion object {
        const val notificationId = 1
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        fun genTitle(ent: ProxyEntity): String {
            val gn = if (DataStore.showGroupInNotification)
                SagerDatabase.groupDao.getById(ent.groupId)?.displayName() else null
            return if (gn == null) ent.displayName() else "[$gn] ${ent.displayName()}"
        }
    }

    var listenPostSpeed = true

    suspend fun postNotificationWakeLockStatus(acquired: Boolean) {
        useBuilder {
            it.priority =
                if (acquired) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW
        }
        update()
    }

    private val showDirectSpeed = DataStore.showDirectSpeed

    private val customView =
        RemoteViews((service as Context).packageName, R.layout.sk_notification_layout).apply {
            setTextViewText(R.id.titleTv, title)
            setTextViewText(R.id.descTv, sk.getString(R.string.sk_open_app_manage_link))
            setImageViewBitmap(R.id.sk_image, BitmapFactory.decodeResource(sk.resources,R.mipmap.sk_ic_launcher))
        }

    private val pendingIntent = PendingIntent.getActivity(
        service as Context,
        0,
        Intent(service, SkSplashActivity::class.java).apply {
            putExtra(StarKnight.ExtraKey.OPEN_TYPE.key, 3)// 加上,点击通知到应用不重新走结果页面
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        },
        flags
    )


    private val builder = NotificationCompat.Builder(service as Context, channel)
        .setSmallIcon(R.mipmap.sk_ic_launcher)
        .setCustomContentView(customView)
        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setPriority(if (visible) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MIN)
        .setContentIntent(pendingIntent)
    private val buildLock = Mutex()

    private suspend fun useBuilder(f: (NotificationCompat.Builder) -> Unit) {
        buildLock.withLock {
            f(builder)
        }
    }

    init {
        service as Context

        service.registerReceiver(this, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })

        runOnMainDispatcher {
            show()
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        if (service.data.state == BaseService.State.Connected) {
            listenPostSpeed = intent.action == Intent.ACTION_SCREEN_ON
        }
    }


    private suspend fun show() =
        useBuilder {
            try {
                if (Build.VERSION.SDK_INT >= 34) {
                    (service as Service).startForeground(
                        notificationId,
                        it.build(),
                        FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
                    )
                } else {
                    (service as Service).startForeground(notificationId, it.build())
                }
            } catch (e: Exception) {
            }
        }

    private suspend fun update() = useBuilder {
        NotificationManagerCompat.from(service as Service).notify(notificationId, it.build())
    }

    fun destroy() {
        listenPostSpeed = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (service as Service).stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            (service as Service).stopForeground(true)
        }
        service.unregisterReceiver(this)
    }
}
