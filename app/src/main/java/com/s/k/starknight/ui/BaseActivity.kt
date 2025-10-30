package com.s.k.starknight.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.StarKnight
import com.s.k.starknight.ad.display.DisplayConfig
import com.s.k.starknight.ad.display.NativeAdViewWrapper
import com.s.k.starknight.ad.pos.AdPos
import com.s.k.starknight.dialog.AdLoadingDialog
import com.s.k.starknight.dialog.AddTimeDialog
import com.s.k.starknight.dialog.IpNotSupportTipDialog
import com.s.k.starknight.dialog.SpeedTestLoadingDialog
import com.s.k.starknight.manager.AppLanguage
import com.s.k.starknight.manager.DialogDisplayManager
import com.s.k.starknight.manager.IpCheckManager
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.aidl.SpeedDisplayData
import io.nekohasekai.sagernet.aidl.TrafficData
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.bg.SagerConnection
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

abstract class BaseActivity : AppCompatActivity(), AppLanguage.OnLanguageChangeCallback, SagerConnection.Callback {
    private val TAG = "BaseActivity"
    val ad by lazy { ActivityAd() }

    var isVisibleActivity: Boolean = true
        private set

    val connection = SagerConnection(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND, true)

    val addTimeDialog by lazy { AddTimeDialog(this) }

    abstract fun onRootView(): View

    protected open fun callbackIpAllowState(isAllowState: Boolean) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        sk.language.setContextLanguage(this)
        super.onCreate(savedInstanceState)
        adapterScreen()
        setContentView(onRootView())
        addReturnCallback()
        sk.language.addLanguageChangeCallback(this)
        lifecycle.addObserver(ad)
        connection.connect(this, this)
    }

    /**
     * 中国大陆地区不允许使用
     */
    private fun handleIp() {
        val state = IpCheckManager.getAllowState()
        if (state == null) {
            IpCheckManager.checkIp {
                if (!it) {
                    showIpForbiddenDialog()
                }
                callbackIpAllowState(it)
            }
        } else {
            if (!state) {
                showIpForbiddenDialog()
            }
            callbackIpAllowState(state)
        }
    }

    fun showIpForbiddenDialog() {
        if (!this.isDestroyed) {
            IpNotSupportTipDialog(this).show()
        }
    }

    override fun onResume() {
        super.onResume()
        isVisibleActivity = true
        Utils.logDebugI("BaseActivity", "activity is $this")
        val state = IpCheckManager.getAllowState()
        if (state != null && !state) {
            showIpForbiddenDialog()
            if (Utils.isConnectedState()) {
                StarKnight.stopService()
            }
        } else {
            if (this !is SkSplashActivity && DataStore.remainTime <= sk.remoteConfig.countDownLeftTime && sk.lifecycle.isAppVisible && sk.user.isVip()) {
                Utils.logDebugI(TAG, "-------------")

                if (isFinishing || isDestroyed) {
                    Utils.logDebugI(TAG, "Activity invalid")
                    return
                }

                Utils.logDebugI(TAG, "onResume check remain < 30 Activity add time")
                DialogDisplayManager.tryShowDialog(this) {
                    addRemainTimeUpdateUi(DataStore.remainTime)
                }
            }
        }
    }

    override fun onStart() {
        connection.updateConnectionId(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND)
        super.onStart()
    }

    override fun onPause() {
        isVisibleActivity = false
        super.onPause()
    }

    override fun onStop() {
        connection.updateConnectionId(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_BACKGROUND)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.disconnect(this)
        DialogDisplayManager.dismiss()
        sk.language.removeLanguageChangeCallback(this)
    }

    private fun adapterScreen() {
        resources.displayMetrics.let {
            val heightScale = it.heightPixels / 818f
            it.scaledDensity = heightScale
            it.density = heightScale
            it.densityDpi = (160 * heightScale).toInt()
        }
    }

    private fun addReturnCallback() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onReturnActivity()
            }
        })
    }

    override fun onLanguageChange() {
        recreate()
    }

    protected open fun onReturnActivity() {
        if (Utils.isConnectedState()) {
            ad.displayReturnAd {
                finish()
            }
        } else {
            finish()
        }
    }

    protected open fun isDisplayReturnAd(): Boolean {
        return false
    }

    protected open fun onCreatePreRequestPosList(): List<String>? {
        return null
    }

    protected open fun onResumePreRequestPosList(): List<String>? {
        return null
    }

    protected open fun onFullScreenDisplayPos(): String? {
        return null
    }

    protected open fun onDisplayNativeInfo(): Pair<String, NativeAdViewWrapper>? {
        return null
    }

    protected open fun onCallPreRequestPosList(): List<String>? {
        return null
    }

    protected open fun needShowNative(): Boolean {
        return true
    }

    protected fun setActivityEdge() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT)
        )
    }

    protected fun setApplyWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            if (view == onRootView()) {
                view.setPadding(
                    systemBars.left, systemBars.top, systemBars.right, systemBars.bottom
                )
            } else {
                onRootView().setPadding(
                    systemBars.left, 0, systemBars.right, systemBars.bottom
                )
                view.setPadding(0, systemBars.top, 0, 0)
            }
            insets
        }
    }


    inner class ActivityAd : DefaultLifecycleObserver {

        private var nativeJob: Job? = null

        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            onCreatePreRequestPosList()?.let {
                sk.ad.preRequestAd(it)
            }
            if (isDisplayReturnAd()) {
                if (Utils.isConnectedState()) {
                    sk.ad.preRequestAd(sk.ad.returnInterstitial)
                }
            }
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            requestNativeAd()
            onResumePreRequestPosList()?.let {
                sk.ad.preRequestAd(it)
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            nativeJob?.cancel()
            nativeJob = null
        }

        private fun requestNativeAd() {
            if (nativeJob?.isActive == true) return
            onDisplayNativeInfo()?.let {
                nativeJob = sk.scope.launch {
                    delay(230)
                    withContext(Dispatchers.Main) {
                        requestDisplayNativeAd(it.first, it.second)
                    }
                }
            }
        }

        private fun requestDisplayNativeAd(pos: String, adView: NativeAdViewWrapper) {
            sk.ad.requestAd(pos) {
                it.displayNativeAd(DisplayConfig(this@BaseActivity).setNativeAdView(adView), needShowNative())
            }
        }

        fun preRequestAd(pos: String){
            sk.ad.preRequestAd(pos)
        }

        fun preRequestAd() {
            onCallPreRequestPosList()?.let {
                sk.ad.preRequestAd(it)
            }
        }

        fun requestAd(pos: String, callback: (AdPos) -> Unit) {
            sk.ad.requestAd(pos, callback)
        }

        fun displayFullScreenAd(isLog: Boolean, callback: () -> Unit) {
            val pos = onFullScreenDisplayPos()
            if (pos.isNullOrEmpty()) {
                Log.d("AdManager", "show: adPos is null")
                callback.invoke()
                return
            }
            displayAd(pos, isLog, callback)
        }

        fun displayAd(pos: String, isLog: Boolean, callback: () -> Unit) {
            if (isLog) {
                sk.event.log("qui_reach_$pos")
            }
            sk.ad.displayAd(pos, DisplayConfig(this@BaseActivity).setCloseCallback(callback))
        }

        fun displayAd(pos: AdPos, isLog: Boolean, callback: () -> Unit) {
            if (isLog) {
                sk.event.log("qui_reach_${pos.adPos}")
            }
            pos.displayAd(DisplayConfig(this@BaseActivity).setCloseCallback(callback))
        }

        fun displayReturnAd(callback: () -> Unit) {
            if (isDisplayReturnAd()) {
                requestLoadingCheckCacheAd(sk.ad.returnInterstitial, callback)
                return
            }
            callback.invoke()
        }

        fun requestLoadingAd(pos: String, callback: () -> Unit) {
            var isLoadFinish = false
            var isTimeoutCloseLoading = false
            val loadingDialog = AdLoadingDialog(this@BaseActivity, 10000L, {
                if (!isLoadFinish) {
                    isTimeoutCloseLoading = true
                    callback.invoke()
                }
            })
            loadingDialog.show()

            sk.ad.requestAd(pos) {
                isLoadFinish = true
                loadingDialog.closeDialog()
                if (!isTimeoutCloseLoading) {
                    displayAd(it, true, callback)
                }
            }
        }

        fun requestLoadingCheckCacheAd(pos: String, callback: () -> Unit) {
            if (!isVisibleActivity) {
                callback.invoke()
                return
            }
            val adPos = sk.ad.getAdPos(pos)
            val ad = adPos.getAd()
            if (ad != null) {
                displayAd(adPos, true, callback)
            } else {
                var isLoadFinish = false
                var isTimeoutCloseLoading = false
                val loadingDialog = AdLoadingDialog(this@BaseActivity, 8000L, {
                    if (!isLoadFinish) {
                        isTimeoutCloseLoading = true
                        callback.invoke()
                    }
                })
                loadingDialog.show()

                sk.ad.requestAd(pos) {
                    isLoadFinish = true
                    loadingDialog.closeDialog()
                    if (!isTimeoutCloseLoading) {
                        displayAd(it, true, callback)
                    }
                }
            }
        }

        /**
         * 主要是该页面的loading弹窗不一样
         */
        fun requestSpeedTestHasCacheAd(pos: String, callback: () -> Unit) {
            if (!isVisibleActivity) {
                callback.invoke()
                return
            }
            val adPos = sk.ad.getAdPos(pos)
            val ad = adPos.getAd()
            if (ad != null) {
                if (BuildConfig.DEBUG) {
                    Log.i("AdManager", "speed test result page has cache ad")
                }
                SpeedTestLoadingDialog(this@BaseActivity, Random.nextInt(3, 5) * 1000L, {
                    displayAd(adPos, true, callback)
                })
            } else {
                var isLoadFinish = false
                var isTimeoutCloseLoading = false
                val loadingDialog = SpeedTestLoadingDialog(this@BaseActivity, 8000L, {
                    if (!isLoadFinish) {
                        isTimeoutCloseLoading = true
                        callback.invoke()
                    }
                })
                loadingDialog.show()

                sk.ad.requestAd(pos) {
                    isLoadFinish = true
                    loadingDialog.closeDialog()
                    if (!isTimeoutCloseLoading) {
                        displayAd(it, true, callback)
                    }
                }
            }
        }

        fun displayRewardInterstitialAd(
            pos: AdPos,
            isLog: Boolean,
            callback: () -> Unit,
            earnedRewardCallback: () -> Unit
        ) {
            if (isLog) {
                sk.event.log("qui_reach_${pos.adPos}")
            }
            pos.displayAd(DisplayConfig(this@BaseActivity).setCloseCallback(callback).setEarnedReward(earnedRewardCallback))
        }

        fun requestLoadingCheckRewardCacheAd(
            pos: String,
            callback: (Boolean) -> Unit,//是否需要展示重试
            earnedRewardCallback: () -> Unit
        ) {
            if (!isVisibleActivity) {
                callback.invoke(false)
                return
            }
            val adPos = sk.ad.getAdPos(pos)
            val ad = adPos.getAd()
            if (ad != null) {
                val tempCallback = {
                    callback.invoke(false)
                }
                displayRewardInterstitialAd(adPos, true, tempCallback, earnedRewardCallback)
            } else {
                var isLoadFinish = false
                var isTimeoutCloseLoading = false
                val loadingDialog = AdLoadingDialog(this@BaseActivity, 10000L, {
                    if (!isLoadFinish) {
                        isTimeoutCloseLoading = true
                        callback.invoke(true)
                    }
                })
                loadingDialog.show()

                sk.ad.requestAd(pos) {
                    isLoadFinish = true
                    loadingDialog.closeDialog()
                    if (!isTimeoutCloseLoading) {
                        val cacheAd = adPos.getAd()
                        if (cacheAd != null) {
                            val tempCallback = {
                                callback.invoke(false)
                            }
                            displayRewardInterstitialAd(adPos, true, tempCallback, earnedRewardCallback)
                        } else {
                            callback.invoke(true)
                        }
                    }
                }
            }
        }

    }

    //////////////////vpn连接监听//////////////////
    override fun onServiceConnected(service: ISagerNetService) {
        Utils.logDebugI(TAG, "onServiceConnected")
        serviceConnected(service)
    }

    protected open fun serviceConnected(service: ISagerNetService) {}

    override fun onServiceDisconnected() {
        Utils.logDebugI(TAG, "onServiceDisconnected")
        serviceDisconnected()
    }

    protected open fun serviceDisconnected() {}

    override fun onBinderDied() {
        connection.disconnect(this)
        connection.connect(this, this)
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        Utils.logDebugI(TAG, "stateChanged")
        if (state == BaseService.State.Connected && sk.user.isVip()) {
            Utils.logDebugI(TAG, "init admob ad")
            sk.initAd()
        }
        onStateChanged(state, profileName, msg)
    }

    protected open fun onStateChanged(state: BaseService.State, profileName: String?, msg: String?) {}

    override fun cbSpeedUpdate(stats: SpeedDisplayData) {
        onCbSpeedUpdate(stats)
    }

    protected open fun onCbSpeedUpdate(stats: SpeedDisplayData) {}

    override fun cbTrafficUpdate(data: TrafficData) {
        runOnDefaultDispatcher {
            ProfileManager.postUpdate(data)
        }
    }

    override fun cbSelectorUpdate(id: Long) {
        val old = DataStore.selectedProxy
        DataStore.selectedProxy = id
        DataStore.currentProfile = id
        runOnDefaultDispatcher {
            ProfileManager.postUpdate(old, true)
            ProfileManager.postUpdate(id, true)
        }
    }

    override fun countDown(time: Long, millis: Long) {
        onCountDown(time, millis)
    }

    private fun onCountDown(time: Long, millis: Long) {
        addRemainTimeUpdateUi(time)
        if (time == sk.remoteConfig.countDownLeftTime && sk.lifecycle.isAppVisible && sk.user.isVip()) {
            Utils.logDebugI(TAG, "-------------millis:$millis")

            if (isFinishing || isDestroyed) {
                Utils.logDebugI(TAG, "Activity invalid")
                return
            }

            DialogDisplayManager.tryShowDialog(this) {
                addRemainTimeUpdateUi(DataStore.remainTime)
            }
        }
    }

    protected open fun addRemainTimeUpdateUi(time: Long) {}

}