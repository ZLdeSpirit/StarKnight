package com.s.k.starknight.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.StarKnight
import com.s.k.starknight.ad.pos.AdPos
import com.s.k.starknight.databinding.SkActivitySplashBinding
import com.s.k.starknight.entity.LastConfig
import com.s.k.starknight.entity.ServerEntity
import com.s.k.starknight.manager.IpCheckManager
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.ui.VpnRequestActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class SkSplashActivity : BaseActivity(){
    private val TAG = "SkSplashActivity"
    private val mBinding by lazy { SkActivitySplashBinding.inflate(layoutInflater) }

    private var openType = -1
    private val handler = Handler(Looper.getMainLooper())
    private val timeOutRunnable = Runnable {
        displayAd(null)
    }
    private var isDisplayAd = true

    private var countDown: CountDownTimer? = null

    private val connect = registerForActivityResult(VpnRequestActivity.StartService()) {
        if (BuildConfig.DEBUG) {
            Log.i("Splash", "----------，isConnect:${Utils.isConnectedState()}-----$it")
        }
        if (it){
            sk.event.log("sk_spla_cancel_per")
            requestAd()
        }else{
            sk.event.log("sk_spla_per_succ")
            startCountDown()
        }
    }

    private fun startCountDown(){
        // 启动倒计时等待连接，连接成功或者失败，则取消倒计时，否则由倒计时进入主页
        countDown = object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Utils.logDebugI(TAG, "startCountDown() onTick() millisUntilFinished = " +
                        "$millisUntilFinished")
            }

            override fun onFinish() {
                Utils.logDebugI(TAG, "startCountDown() onFinish()")
                requestAd()
            }
        }
        countDown?.start()
    }

    override fun onCallPreRequestPosList(): List<String>? {
        return if (sk.preferences.isSetAppLanguage) {
            listOf(sk.ad.homeNative, sk.ad.connectedInterstitial, sk.ad.homeInterstitial, sk.ad.disconnectSuccessInterstitial)
        } else {
            listOf(sk.ad.languageNative, sk.ad.languageInterstitial)
        }
    }

    override fun onCreatePreRequestPosList(): List<String>? {
        if (sk.preferences.isSetAppLanguage){
            return arrayListOf(sk.ad.connectedInterstitial,sk.ad.disconnectSuccessInterstitial, sk.ad.homeNative, sk.ad.homeInterstitial)
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

        sk.event.log("sk_reach_${onFullScreenDisplayPos()}")
        if (openType == 1) {
            sk.event.log("sk_clk_msg")
        }
        when(openType){
            3 -> {// 点击连接vpn时的通知
                sk.event.log("sk_clk_conn_noti")
            }
            4 -> {//点击vpn断开时的通知
                sk.event.log("sk_clk_disconn_noti")
            }
            5 -> {// 热启动
                sk.event.log("sk_hot_start")
            }
        }
        sk.event.log("sk_op_open", Bundle().apply {
            putString(
                "type", when (openType) {
                    3 -> "sk_clk_conn_noti"
                    4 -> "sk_clk_disconn_noti"
                    5 -> "sk_hot_start"
                    else -> "oth"
                }
            )
        })
    }

    private fun handleIsFirstSplash() {
        if (sk.user.isVip()){
            if (Utils.isConnectedState()){
                requestAd()
            }else{
                sk.serverConfig.setDefaultConfig()
                connect.launch(null)
            }
        }else{
            requestAd()
        }
    }

    private fun requestAd() {
        handler.postDelayed(timeOutRunnable, 16300)
        ad.requestAd(onFullScreenDisplayPos()) {
            if (!ad.isMatchCondition()){
                lifecycleScope.launch {
                    delay(2000)
                    handler.removeCallbacks(timeOutRunnable)
                    skipActivity()
                }
            }else {
                handler.removeCallbacks(timeOutRunnable)
                displayAd(it)
            }
        }
        ad.preRequestAd()
    }

    private fun displayAd(adPos: AdPos?) {
        if (!isDisplayAd) return
        isDisplayAd = false
        if (adPos != null) {
            ad.displayAd(adPos, true) {
                skipActivity()
            }
        } else {
            ad.displayAd(onFullScreenDisplayPos(), true) {
                skipActivity()
            }
        }
    }

    private fun skipActivity() {
        if (isDestroyed) return
//        if (openType == 2 || (!isVisibleActivity && !sk.lifecycle.isAppVisible)) {
//            Utils.logDebugI("SplashActivity", "type = 2  finish")
//            finish()
//            return
//        }

        // 热启动和外部断开的通知都被认为是4
        if (openType == 4 || openType == 5 || (!isVisibleActivity && !sk.lifecycle.isAppVisible)){
            Utils.logDebugI("SplashActivity", "type = 2  finish")
            // app进入后台，断掉时
            startActivity(
                Intent(
                    this,MainActivity::class.java
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.extras?.let {
                        putExtras(it)
                    }
                }
            )
            finish()
            return
        }

        Utils.logDebugI("SplashActivity", "skipActivity()")
        val state = IpCheckManager.getAllowState()
        if (state != null && !state) {
            handleIpForbidden()
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeOutRunnable)
    }

    private fun requestConsentInfoUpdate() {
        if (!sk.isRequestUmp) {
            handleIsFirstSplash()
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
                        handleIsFirstSplash()
                    }
                }
            },
            {
                sk.isRequestUmp = false
                handleIsFirstSplash()
            })
    }

    var currentState = BaseService.State.Idle
    override fun onStateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.i("Splash", "----state:${state}------，isConnect:${Utils.isConnectedState()}")
        }
        if (state == BaseService.State.Connected) {
            Utils.logDebugI(TAG, "onStateChanged() connected success state = $state")
            sk.event.log("sk_conn_succ_")
            countDown?.cancel()
            requestAd()
        }else{
            // 失败
            if ((currentState == BaseService.State.Connecting && state == BaseService.State.Stopping)
                ||
                (currentState == BaseService.State.Connecting && state == BaseService.State.Stopped)){
                Utils.logDebugI(TAG, "onStateChanged() connected failed state = $state")
                countDown?.cancel()
                lifecycleScope.launch {
                    delay(2000)
                    skipActivity()
                }
            }

        }
        currentState = state
    }

}