package com.s.k.starknight

import android.app.Application
import android.util.Base64
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
import com.s.k.starknight.manager.UploadAdValue
import com.s.k.starknight.manager.UploadEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject
import kotlin.compareTo

lateinit var sk: StarKnight
    private set
class StarKnight : Application(){

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
    val language by lazy { AppLanguage() }
    val preferences by lazy { AppPreferences() }

    val user by lazy { AppUserAttr() }

    val adValue by lazy { UploadAdValue() }

    val event by lazy { UploadEvent() }

    val remoteConfig by lazy { RemoteConfig() }

    val db by lazy {
        Room.databaseBuilder(this, SkDatabase::class.java, "sk_database").allowMainThreadQueries().build()
    }

    val ad by lazy { AdManager() }

    val lifecycle by lazy { AppActivityLifecycle() }

    var isRequestUmp = true

    override fun onCreate() {
        super.onCreate()
        sk = this
        language.setContextLanguage(this)
        if (preferences.quickOpenTime <= 0) {
            preferences.quickOpenTime = System.currentTimeMillis()
        }
        initFirebase()
        remoteConfig.fetchAndActivate()
        event.initUserUserProperty()
        user.initUserAttr()
        initFacebook()
        initAd()
//        notify.uploadToken(false)
        registerActivityLifecycleCallbacks(lifecycle)
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
        CODE_PATH("sk_code_path"),
        CODE_IMAGE_PATH("sk_code_image_path"),
        CODE_CONTENT("sk_code_content"),
        CODE_TYPE("sk_code_type"),
        FINAL_CODE_PATH("sk_final_code_path"),
        COVER_URL("sk_cover_url"),
        PARSE_URL("sk_parse_url"),
        PLAY_URL("sk_play_url"),
        NOTIFY_ID("sk_notify_id"),
        CODE_TYPE_INFO("sk_code_type_info")
    }
}