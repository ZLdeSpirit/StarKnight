package com.s.k.starknight.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.os.SystemClock
import android.provider.Settings
import android.text.format.DateUtils
import android.text.format.Formatter
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.ToastUtils
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.R
import com.s.k.starknight.StarKnight
import com.s.k.starknight.ad.display.NativeAdViewWrapper
import com.s.k.starknight.databinding.SkActivityMainBinding
import com.s.k.starknight.dialog.PermissionDialog
import com.s.k.starknight.dialog.RewardRetryDialog
import com.s.k.starknight.entity.LastConfig
import com.s.k.starknight.manager.DialogDisplayManager
import com.s.k.starknight.sk
import com.s.k.starknight.tools.FreqOperateLimit
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.aidl.SpeedDisplayData
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.ui.VpnRequestActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : BaseActivity() {
    private val TAG = "MainActivity"
    private val lock = Any()
    private val mBinding by lazy { SkActivityMainBinding.inflate(layoutInflater) }
    private val connect = registerForActivityResult(VpnRequestActivity.StartService()) {
        if (it) ToastUtils.showShort(R.string.sk_vpn_permission_denied)
    }

    private var requestPermissionTime: Long = -1L

    private var readPermissionLauncher: ActivityResultLauncher<String>? = null

    private var openType = -1

    override fun onRootView(): View {
        return mBinding.root
    }

    override fun onDisplayNativeInfo(): Pair<String, NativeAdViewWrapper> {
        return sk.ad.homeNative to mBinding.nativeAdWrapper
    }

    override fun onCreatePreRequestPosList(): List<String>? {
        return arrayListOf(
            sk.ad.homeInterstitial,
            sk.ad.connectedInterstitial,
            sk.ad.disconnectSuccessInterstitial,
            sk.ad.addTimeReward,
            sk.ad.resultInterstitial,
            sk.ad.settingsNative,
            sk.ad.resultNative,
        )
    }

    override fun onCallPreRequestPosList(): List<String>? {
        return arrayListOf(
            sk.ad.homeInterstitial,
            sk.ad.connectedInterstitial,
            sk.ad.disconnectSuccessInterstitial,
            sk.ad.addTimeReward,
            sk.ad.resultInterstitial,
            sk.ad.settingsNative,
            sk.ad.resultNative,
        )
    }

    override fun onResumePreRequestPosList(): List<String>? {
        return arrayListOf(sk.ad.homeInterstitial, sk.ad.addTimeReward)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.logDebugI(TAG, "onCreate")
        initListener()
        initView()
    }

    override fun onResume() {
        try {
            super.onResume()
            //预加载点击插屏
            Utils.logDebugI(TAG, "onResume")

            val lastConfig = sk.preferences.getLastConfig()
            if (lastConfig != null) {
                mBinding.selectServerFlagIv.setImageResource(Utils.getCountryFlag(lastConfig.countryCode))
                mBinding.selectCountryTv.text = lastConfig.name
            } else {
                setDefaultConfig()
            }
            val time = Utils.formatMillis(DataStore.remainTime * 1000)
            mBinding.connectTimeTv.text = time

            if (DataStore.remainTime <= sk.remoteConfig.countDownLeftTime && sk.lifecycle.isAppVisible && sk.user.isVip()) {
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

            if (fromOuterComeNeedShowResultPage){
                fromOuterComeNeedShowResultPage = false
                if (Utils.isConnectedState()) {
                    connectState(BaseService.State.Connected)
                }
            }
        } catch (e: Exception) {
            Utils.logDebugI("MainActivity", "${e.message}")
        }

    }

    private fun initListener() {
        mBinding.apply {
            selectLl.setOnClickListener {
                if (Utils.isConnectedState()) {
                    ad.requestLoadingCheckCacheAd(sk.ad.homeInterstitial) {
                        startActivityForResult(Intent(this@MainActivity, SkSelectServerActivity::class.java).apply {
                            putExtra(SkSelectServerActivity.KEY_FROM, -1)
                        }, 100)
                    }
                } else {
                    startActivityForResult(Intent(this@MainActivity, SkSelectServerActivity::class.java).apply {
                        putExtra(SkSelectServerActivity.KEY_FROM, -1)
                    }, 100)
                }
            }
            mainSettingsIv.setOnClickListener {
                if (Utils.isConnectedState()) {
                    ad.requestLoadingCheckCacheAd(sk.ad.homeInterstitial) {
                        startActivity(Intent(this@MainActivity, SkSettingsActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this@MainActivity, SkSettingsActivity::class.java))
                }
            }
            noConnectIv.setOnClickListener {
                if (DataStore.serviceState.canStop) {
                    stop()
                } else {
                    logConnect("sk_click_connect_btn_")
                    connect.launch(null)

                }
            }
        }
        mBinding.adTimeLl.setOnClickListener {
            // 激励广告
            requestRewardAd()
        }
    }

    private fun stop() {
        if (sk.user.isVip()) {
            // 买量用户点击断开按钮，需要先看广告之后才能断开
            ad.requestLoadingAd(sk.ad.disconnectSuccessInterstitial) {
                StarKnight.Companion.stopService()
            }
            return
        }
        // 普通用户断开时，插屏
        // 普通用户清除连接时的缓存广告
        StarKnight.Companion.stopService()
    }

    private fun logConnect(tag: String) {
        val lastConfig = sk.preferences.getLastConfig()
        if (lastConfig != null) {
            sk.event.log(tag + lastConfig.name)
        } else {
            sk.event.log(tag)
        }
    }

    private fun requestRewardAd() {
        ad.requestLoadingCheckRewardCacheAd(sk.ad.addTimeReward, {
            if (it) {
                RewardRetryDialog(this, {
                    requestRewardAd()
                }).show()
            }
        }, {
            addTime()
        })
    }

    private fun addTime() {
        if (DataStore.serviceState != BaseService.State.Connected) {
            var remainTime = DataStore.remainTime
            remainTime = remainTime + sk.remoteConfig.remainTime
            DataStore.remainTime = remainTime
            val time = Utils.formatMillis(remainTime * 1000)
            mBinding.connectTimeTv.text = time
        } else {
            connection.service?.addTime(sk.remoteConfig.remainTime)
        }
    }

    private fun initView() {
//        checkNotificationPermission()
        openType = intent.getIntExtra(StarKnight.ExtraKey.OPEN_TYPE.key, -1)
        // 进入主页，买量用户在没有连接情况下就直接连接
        if (sk.user.isVip() && !Utils.isConnectedState()) {
            if (BuildConfig.DEBUG) {
                Log.i("MainActivity", "come from buy user auto disconnect || manual back to app")
            }
            if (DataStore.remainTime <= sk.remoteConfig.countDownLeftTime) {
                addTime()
            }
            connect.launch(null)
        }

        mBinding.apply {
            adTimeTv.text = "+" + (sk.remoteConfig.remainTime / 60) + " mins"
        }
    }

    private var fromOuterComeNeedShowResultPage = false

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openType = intent.getIntExtra(StarKnight.ExtraKey.OPEN_TYPE.key, -1)
        fromOuterComeNeedShowResultPage = intent.getBooleanExtra(StarKnight.ExtraKey.IS_JUMP_RESULT.key, false)
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
                    withContext(Dispatchers.Main) {
                        mBinding.selectServerFlagIv.setImageResource(Utils.getCountryFlag(lastConfig.countryCode))
                        mBinding.selectCountryTv.text = lastConfig.name
                    }
                }

            }
        }
    }

    private fun checkNotificationPermission() {
        val toSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

        readPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it && SystemClock.elapsedRealtime() - requestPermissionTime <= 500) {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.setData("package:${packageName}".toUri())
                    toSettingsLauncher.launch(intent)
                    return@registerForActivityResult
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        if (abs(System.currentTimeMillis() - sk.preferences.showOpenMsgTime) < DateUtils.DAY_IN_MILLIS) return
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionDialog(this@MainActivity) {
                try {
                    requestPermissionTime = SystemClock.elapsedRealtime()
                    readPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.show()
        }
    }

    private fun handleAddTimeBtn() {
        if (sk.user.isVip()) {
            mBinding.adTimeLl.isVisible = Utils.isConnectedState()
        } else {
            mBinding.adTimeLl.isVisible = !Utils.isConnectedState()
        }
    }

    private fun connectState(state: BaseService.State) {
        Log.i("MainActivity", "server state:${DataStore.serviceState}")
        when (state) {
            BaseService.State.Connecting -> {
                mBinding.stateDotView.setBackgroundResource(R.drawable.sk_dot_connecting_state)
                SpanUtils.with(mBinding.stateTv)
                    .append(getString(R.string.sk_status))
                    .append(getString(R.string.sk_connecting))
                    .setForegroundColor(getColor(R.color.sk_connecting_state))
                    .create()
                mBinding.connectingLav.isVisible = true
                mBinding.noConnectingLl.isVisible = false
            }

            BaseService.State.Connected -> {
                if (!FreqOperateLimit.doing(this, 500)) {
                    Utils.logDebugI(TAG, "FreqOperateLimit")
                    return
                }
                ad.preRequestAd()
                mBinding.stateDotView.setBackgroundResource(R.drawable.sk_dot_connected_state)
                SpanUtils.with(mBinding.stateTv)
                    .append(getString(R.string.sk_status))
                    .append(getString(R.string.sk_connected))
                    .setForegroundColor(getColor(R.color.sk_connected_state))
                    .create()
                mBinding.connectingLav.isVisible = false
                mBinding.noConnectingLl.isVisible = true
                mBinding.noConnectIv.setImageResource(R.drawable.sk_ic_connected)
                // 连接成功，仅买量用户展示插屏广告
                if (openType != 3) {//=3表示从点击vpn的通知进来，就不走连接成功结果页了，因为从通知进来还会走该状态
                    Utils.logDebugI(TAG, "afdafdafadfafdaf")
                    if (isVisibleActivity) {// 可见时才进入结果页面
                        // 既不是切换服务器也不是切换语言才走
                        if (!Utils.isSwitchServer && !Utils.isChangeLanguage) {
                            Utils.logDebugI(TAG, "234345345345345")
                            if (sk.user.isVip()) {
                                Utils.logDebugI(TAG, "[][]]][]]]")
                                ad.requestLoadingAd(sk.ad.connectedInterstitial) {
                                    Utils.logDebugI(TAG, "4444444")
                                    startActivity(Intent(this@MainActivity, SkResultActivity::class.java))
                                }
                            } else {
                                Utils.logDebugI(TAG, "55555555")
                                startActivity(Intent(this@MainActivity, SkResultActivity::class.java))
                            }
                        } else {
                            Utils.isSwitchServer = false
                            Utils.isChangeLanguage = false
                        }
                    }
                }
            }

            BaseService.State.Stopped -> {
                if (!FreqOperateLimit.doing(lock, 200)) {
                    return
                }
                ad.preRequestAd()
                mBinding.stateDotView.setBackgroundResource(R.drawable.sk_dot_disconnected_state)
                SpanUtils.with(mBinding.stateTv)
                    .append(getString(R.string.sk_status))
                    .append(getString(R.string.sk_disconnected))
                    .setForegroundColor(getColor(R.color.sk_disconnected_state))
                    .create()
                mBinding.connectingLav.isVisible = false
                mBinding.noConnectingLl.isVisible = true
                mBinding.noConnectIv.setImageResource(R.drawable.sk_ic_disconnected)
                mBinding.downloadSpeedTv.text = "--Mbps"
                mBinding.uploadSpeedTv.text = "--Mbps"

                if (isVisibleActivity) {
                    if (!Utils.isSwitchServer) {
                        if (sk.user.isVip()) {
                            startActivity(Intent(this@MainActivity, SkResultActivity::class.java))
                        } else {
                            if (!normalUserNotJumpResultPage) {
                                ad.requestLoadingAd(sk.ad.disconnectSuccessInterstitial) {
                                    Utils.logDebugI(TAG, "33333333")
                                    startActivity(Intent(this@MainActivity, SkResultActivity::class.java))
                                }
                            }else{
                                normalUserNotJumpResultPage = false
                            }
                        }
                    }else{
                        Utils.isSwitchServer = false
                    }
                }
            }

            else -> {}
        }
        handleAddTimeBtn()
    }

    private var normalUserNotJumpResultPage = false
    override fun serviceConnected(service: ISagerNetService) {
        Utils.logDebugI(TAG, "onServiceConnected")
        if (!sk.user.isVip()) {
            normalUserNotJumpResultPage = true
        }
        connectState(
            try {
                BaseService.State.values()[service.state]
            } catch (_: RemoteException) {
                BaseService.State.Idle
            }
        )
    }

    override fun serviceDisconnected() {
        Utils.logDebugI(TAG, "onServiceDisconnected")
        connectState(BaseService.State.Idle)
    }

    override fun onStateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        Utils.logDebugI(TAG, "stateChanged")
        connectState(state)
        if (state == BaseService.State.Connected) {
            sk.event.log("sk_conn_succ_")
        }
    }

    override fun onCbSpeedUpdate(stats: SpeedDisplayData) {
        mBinding.downloadSpeedTv.text = Formatter.formatFileSize(this, stats.rxRateProxy)
        mBinding.uploadSpeedTv.text = Formatter.formatFileSize(this, stats.txRateProxy)
    }

    override fun addRemainTimeUpdateUi(time: Long) {
        val timeStr = Utils.formatMillis(time * 1000)
        mBinding.connectTimeTv.text = timeStr
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null&&resultCode == 200){
            val isNeedConnect = data.getBooleanExtra(SkSelectServerActivity.KEY_NEED_CONNECT, false)
            if (isNeedConnect && !Utils.isConnectedState()){
                connect.launch(null)
            }
        }
    }

}
