package com.s.k.starknight.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.StarKnight
import com.s.k.starknight.ad.pos.AdPos
import com.s.k.starknight.databinding.SkActivitySplashBinding
import com.s.k.starknight.entity.LastConfig
import com.s.k.starknight.manager.IpCheckManager
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.aidl.SpeedDisplayData
import io.nekohasekai.sagernet.aidl.TrafficData
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import io.nekohasekai.sagernet.ui.VpnRequestActivity
import kotlinx.coroutines.launch
import kotlin.random.Random

class SkSplashActivity : BaseActivity(){

    private val mBinding by lazy { SkActivitySplashBinding.inflate(layoutInflater) }

    private var openType = -1
    private val handler = Handler(Looper.getMainLooper())
    private val timeOutRunnable = Runnable {
        displayAd(null)
    }
    private var isDisplayAd = true

    private val connect = registerForActivityResult(VpnRequestActivity.StartService()) {
        if (BuildConfig.DEBUG) {
            Log.i("Splash", "----------")
        }
        requestAd()
    }

    private var isFirstConnectJump = false

    override fun onCreatePreRequestPosList(): List<String>? {
        if (sk.preferences.isSetAppLanguage && Utils.isConnectedState()){
            return arrayListOf(sk.ad.connectedInterstitial,sk.ad.disconnectSuccessInterstitial)
        }else {
            return super.onCreatePreRequestPosList()
        }
    }

    override fun onRootView(): View {
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.logDebugI("Splash", "onCreate")
        setActivityEdge()
        setApplyWindowInsets(onRootView())
        openType = intent.getIntExtra(StarKnight.ExtraKey.OPEN_TYPE.key, -1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                requestConsentInfoUpdate()
            } else {
                val launcher =
                    registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                        requestConsentInfoUpdate()
                    }
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            requestConsentInfoUpdate()
        }
        IpCheckManager.checkIp{
            handleIsFirstSplash(it)
        }
        sk.event.log("sk_reach_${onFullScreenDisplayPos()}")
        if (openType == 1) {
            sk.event.log("sk_clk_msg")
        }
        sk.event.log("sk_op_open", Bundle().apply {
            putString(
                "type", when (openType) {
                    1 -> "msg"
                    2 -> "hot"
                    3 -> "vpn msg"
                    else -> "oth"
                }
            )
        })
    }

    private fun handleIsFirstSplash(isAllowState: Boolean) {
        if (sk.preferences.isFirstSplash) {
            if (isAllowState) {
                if (sk.user.isVip()) {
                    Utils.logDebugI("Splash", "handleIsFirstSplash")
                    setDefaultConfig()
                    connect.launch(null)
                    isFirstConnectJump = true
                } else {
                    requestAd()
                }
            }else{
                showIpForbiddenDialog()
            }
        } else {
            requestAd()
        }
    }

    private fun setDefaultConfig() {
        // 初始化editingId，默认就是0，实际上该值为插入数据库的id
        DataStore.editingId = 0
        DataStore.editingGroup = DataStore.selectedGroupForImport()
        val list = sk.serverConfig.getServerConfig()
        if (list.isNotEmpty()) {
            val serverEntity = list[0]
            serverEntity.apply {
                val editingGroup = DataStore.editingGroup
                val lastConfig = LastConfig(countryParseName, countryCode, socksBeanList)
                sk.preferences.setLastConfig(lastConfig)
                val randomIndex = Random.nextInt(0, socksBeanList.size)
                val socksBean = socksBeanList[randomIndex]
                sk.scope.launch {
                    val proxyEntity = ProfileManager.createProfile(editingGroup, socksBean)
                    DataStore.selectedProxy = proxyEntity.id
                }

            }
        }
    }

    private fun requestAd() {
        handler.postDelayed(timeOutRunnable, 16300)
        ad.requestAd(onFullScreenDisplayPos()) {
            handler.removeCallbacks(timeOutRunnable)
            displayAd(it)
        }
    }

    private fun displayAd(adPos: AdPos?) {
        if (!isDisplayAd) return
        isDisplayAd = false
        if (adPos != null) {
            checkDisplayAd {
                if (it) {
                    ad.displayAd(adPos, false) {
                        skipActivity()
                    }
                } else {
                    skipActivity()
                }
            }

        } else {
            checkDisplayAd {
                if (it) {
                    ad.displayAd(onFullScreenDisplayPos(), false) {
                        skipActivity()
                    }
                } else {
                    skipActivity()
                }
            }

        }
    }

    private fun checkDisplayAd(callback: (Boolean) -> Unit) {
        if (sk.user.isVip()) {//买量用户在连接时才展示
            callback.invoke(DataStore.serviceState == BaseService.State.Connected)
        } else {// 普通用户未连接时才展示
            callback.invoke(DataStore.serviceState != BaseService.State.Connected)
        }
    }

    private fun skipActivity() {
        if (isDestroyed) return
        if (openType == 2 || (!isVisibleActivity && !sk.lifecycle.isAppVisible)) {
            finish()
            return
        }
        val isSetLanguage = sk.preferences.isSetAppLanguage
        startActivity(
            Intent(
                this,
                if (isSetLanguage) MainActivity::class.java else SkLanguageActivity::class.java
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.extras?.let {
                    putExtras(it)
                }
            }
        )
        finish()
    }

    override fun onFullScreenDisplayPos(): String {
        return sk.ad.open
    }

    override fun onReturnActivity() {

    }

    override fun onCallPreRequestPosList(): List<String>? {
        return if (sk.preferences.isSetAppLanguage) {
            listOf(sk.ad.homeNative)
        } else {
            listOf(sk.ad.languageNative)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeOutRunnable)
    }

    private fun requestConsentInfoUpdate() {
        if (!sk.isRequestUmp) {
//            requestAd()
            return
        }
        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { _ ->
                    sk.isRequestUmp = false
                    if (consentInformation.canRequestAds()) {
                    }
//                    requestAd()
                }
            },
            {
                sk.isRequestUmp = false
//                requestAd()
            })
    }

}