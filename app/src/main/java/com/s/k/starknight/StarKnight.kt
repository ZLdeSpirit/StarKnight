package com.s.k.starknight

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.UiModeManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.PowerManager
import android.os.StrictMode
import android.os.UserManager
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.room.Room
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.crashlytics
import com.reyun.solar.engine.SolarEngineManager
import com.s.k.starknight.ad.AdManager
import com.s.k.starknight.config.RemoteConfig
import com.s.k.starknight.db.SkDatabase
import com.s.k.starknight.lifecycle.AppActivityLifecycle
import com.s.k.starknight.manager.AppLanguage
import com.s.k.starknight.manager.AppPreferences
import com.s.k.starknight.manager.AppUserAttr
import com.s.k.starknight.manager.IpCheckManager
import com.s.k.starknight.manager.NotifyManager
import com.s.k.starknight.manager.ServerConfigManager
import com.s.k.starknight.manager.UploadAdValue
import com.s.k.starknight.manager.UploadEvent
import com.s.k.starknight.tools.Utils
import com.s.k.starknight.ui.SkSplashActivity
import go.Seq
import io.nekohasekai.sagernet.Action
import io.nekohasekai.sagernet.bg.SagerConnection
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ktx.Logs
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import io.nekohasekai.sagernet.utils.CrashHandler
import io.nekohasekai.sagernet.utils.DefaultNetworkListener
import io.nekohasekai.sagernet.utils.PackageCache
import io.nekohasekai.sagernet.utils.Theme
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import libcore.Libcore
import moe.matsuri.nb4a.NativeInterface
import moe.matsuri.nb4a.net.LocalResolverImpl
import moe.matsuri.nb4a.utils.JavaUtil
import moe.matsuri.nb4a.utils.cleanWebview
import org.json.JSONObject
import java.io.File
import androidx.work.Configuration as WorkConfiguration

lateinit var sk: StarKnight
    private set

class StarKnight : Application(), WorkConfiguration.Provider {
    val solar: SolarEngineManager
        get() {
            return SolarEngineManager.getInstance()
        }

    val isInitSolar: Boolean
        get() {
            return solar.initialized.get()
        }

    val scope =
        CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
            Firebase.crashlytics.recordException(throwable)
        })

    val serverConfig = ServerConfigManager()
    val language by lazy { AppLanguage() }
    val preferences by lazy { AppPreferences() }

    val user by lazy { AppUserAttr() }

    val adValue by lazy { UploadAdValue() }

    val event by lazy { UploadEvent() }

    val notify by lazy { NotifyManager() }

    val remoteConfig by lazy { RemoteConfig() }

    val db by lazy {
        Room.databaseBuilder(this, SkDatabase::class.java, "sk_database").allowMainThreadQueries().build()
    }

    val ad by lazy { AdManager() }

    val lifecycle by lazy { AppActivityLifecycle() }

    var isRequestUmp = true

    //////////////////////////////////
    private val nativeInterface = NativeInterface()
    val externalAssets: File by lazy { getExternalFilesDir(null) ?: filesDir }
    val process: String = JavaUtil.getProcessName()
    private val isMainProcess = process == BuildConfig.APPLICATION_ID
    val isBgProcess = process.endsWith(":bg")
    val power by lazy { getSystemService<PowerManager>()!! }
    val connectivity by lazy { getSystemService<ConnectivityManager>()!! }

    val intervalStartTime by lazy { remoteConfig.intervalStartTime }
    val intervalEndTime by lazy { remoteConfig.intervalEndTime }

    var underlyingNetwork: Network? = null

    /////////////////////////////////
    override fun onCreate() {
        super.onCreate()
        Utils.logDebugI("StarKnight_init", "onCreate----------")
        sk = this
        application = this
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler)
        if (isMainProcess) {
            language.setContextLanguage(this)
            notify.uploadToken(false)
            registerActivityLifecycleCallbacks(lifecycle)
            if (preferences.quickOpenTime <= 0) {
                preferences.quickOpenTime = System.currentTimeMillis()
            }
            IpCheckManager.checkIp{}
        }

        initFirebase()
        initFacebook()
        remoteConfig.fetchAndActivate()
        user.initUserAttr()
        event.initUserUserProperty()
        initSagerNet()

        if (BuildConfig.DEBUG) {
            System.setProperty("StarKnight", "StarKnight")
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }

    private fun initFirebase() {
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initAd() {
        try {
            MobileAds.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * {
     *   "sk_fb_app_id":"123455",
     *   "sk_fb_app_token":"12345"
     * }
     */
    fun initFacebook() {
        val config = remoteConfig.facebookInfoConfig
        if (config.isEmpty()) return
        try {
            val json = JSONObject(String(Base64.decode(config, Base64.NO_WRAP)))
            val id = json.getString("sk_fb_app_id")
            if (id.isEmpty()) return
            val token = json.getString("sk_fb_app_token")
            if (token.isEmpty()) return
            initFacebook(id, token)
            user.initSolar(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initFacebook(id: String, token: String) {
        FacebookSdk.setClientToken(token)
        FacebookSdk.setApplicationId(id)
        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)
        adValue.uploadSaveShowAdValueToFacebook()
        adValue.uploadSaveClickAdValueToFacebook()
    }

    enum class ExtraKey(val key: String) {
        OPEN_TYPE("sk_open_type"),
        NOTIFY_ID("sk_notify_id"),
        IS_FOREGROUND("sk_is_foreground"),
        IS_JUMP_RESULT("is_jump_result")
    }

    enum class ExtraValue(val value: Int){
        ADD_TIME_AND_CONNECT(4),
        IS_ADD_TIME_AND_CONNECT(5),
    }


    ////////////////////////////
    private fun initSagerNet(){
        if (isMainProcess || isBgProcess) {
            externalAssets.mkdirs()
            Seq.setContext(this)
            Libcore.initCore(
                process,
                cacheDir.absolutePath + "/",
                filesDir.absolutePath + "/",
                externalAssets.absolutePath + "/",
                DataStore.logBufSize,
                DataStore.logLevel > 0,
                nativeInterface, nativeInterface, LocalResolverImpl
            )

            // fix multi process issue in Android 9+
            JavaUtil.handleWebviewDir(this)

            runOnDefaultDispatcher {
                PackageCache.register()
                cleanWebview()
            }
        }

        if (isMainProcess) {
            Theme.applyNightTheme()
            runOnDefaultDispatcher {
                DefaultNetworkListener.start(this) {
                    underlyingNetwork = it
                }

                updateNotificationChannels()
            }
        }
    }

    fun updateNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) @RequiresApi(26) {
            notification.createNotificationChannels(
                listOf(
                    NotificationChannel(
                        "service-vpn",
                        getText(R.string.sk_service_vpn),
                        if (Build.VERSION.SDK_INT >= 28) NotificationManager.IMPORTANCE_MIN
                        else NotificationManager.IMPORTANCE_LOW
                    ),   // #1355

                )
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun getWorkManagerConfiguration(): WorkConfiguration {
        return WorkConfiguration.Builder()
            .setDefaultProcessName("${BuildConfig.APPLICATION_ID}:bg")
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        Libcore.forceGc()
    }

    @SuppressLint("InlinedApi")
    companion object {

        lateinit var application: StarKnight

        val isTv by lazy {
            uiMode.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        }

        val configureIntent: (Context) -> PendingIntent by lazy {
            {
                PendingIntent.getActivity(
                    it,
                    0,
                    Intent(
                        application, SkSplashActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
            }
        }
        val activity by lazy { application.getSystemService<ActivityManager>()!! }
        val clipboard by lazy { application.getSystemService<ClipboardManager>()!! }
        val connectivity by lazy { application.getSystemService<ConnectivityManager>()!! }
        val notification by lazy { application.getSystemService<NotificationManager>()!! }
        val user by lazy { application.getSystemService<UserManager>()!! }
        val uiMode by lazy { application.getSystemService<UiModeManager>()!! }
        val power by lazy { application.getSystemService<PowerManager>()!! }

        fun getClipboardText(): String {
            return clipboard.primaryClip?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)?.text?.toString() ?: ""
        }

        fun trySetPrimaryClip(clip: String) = try {
            clipboard.setPrimaryClip(ClipData.newPlainText(null, clip))
            true
        } catch (e: RuntimeException) {
            Logs.w(e)
            false
        }

        fun startService() = ContextCompat.startForegroundService(
            application, Intent(application, SagerConnection.serviceClass)
        )

        fun reloadService() =
            application.sendBroadcast(Intent(Action.RELOAD).setPackage(application.packageName))

        fun stopService() =
            application.sendBroadcast(Intent(Action.CLOSE).setPackage(application.packageName))

        var underlyingNetwork: Network? = null

        var appVersionNameForDisplay = {
            var n = BuildConfig.VERSION_NAME
//            if (isPreview) {
//                n += " " + BuildConfig.PRE_VERSION_NAME
//            } else if (!isOss) {
//                n += " ${BuildConfig.FLAVOR}"
//            }
//            if (BuildConfig.DEBUG) {
//                n += " DEBUG"
//            }
            n
        }()
    }
}