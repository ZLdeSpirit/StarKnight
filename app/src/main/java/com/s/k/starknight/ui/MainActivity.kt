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

    // 用来连接失败埋点的
    private var isCLickConnect = false

    private var openType = -1


    override fun onRootView(): View {
        return mBinding.root
    }

    override fun onDisplayNativeInfo(): Pair<String, NativeAdViewWrapper> {
        return sk.ad.homeNative to mBinding.nativeAdWrapper
    }

    override fun needShowNative(): Boolean {
        if (sk.user.isVip()) {
            return Utils.isConnectedState()
        } else {
            return !Utils.isConnectedState()
        }
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
            if (Utils.isConnectedState()) {
                sk.ad.preRequestAd(sk.ad.homeInterstitial)
                if (sk.user.isVip()) {
                    sk.ad.preRequestAd(sk.ad.addTimeReward)
                }
            }

            val lastConfig = sk.preferences.getLastConfig()
            if (lastConfig != null) {
                mBinding.selectServerFlagIv.setImageResource(Utils.getCountryFlag(lastConfig.countryCode))
                mBinding.selectCountryTv.text = lastConfig.name
            } else {
                setDefaultConfig()
            }
            val time = Utils.formatMillis(DataStore.remainTime * 1000)
            mBinding.connectTimeTv.text = time
        } catch (e: Exception) {
            Utils.logDebugI("MainActivity", "${e.message}")
        }

    }

    private fun initListener() {
        mBinding.apply {
            selectLl.setOnClickListener {
                if (Utils.isConnectedState()) {
                    ad.requestLoadingCheckCacheAd(sk.ad.homeInterstitial) {
                        startActivity(Intent(this@MainActivity, SkSelectServerActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this@MainActivity, SkSelectServerActivity::class.java))
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
                    isCLickConnect = true
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
                Utils.logDebugI(TAG, "11111111")
                startActivity(Intent(this@MainActivity, SkResultActivity::class.java))
            }
            return
        }
        // 普通用户断开时，插屏
        // 普通用户清除连接时的缓存广告
        sk.ad.clearCacheAd(true)
        ad.requestLoadingAd(sk.ad.disconnectSuccessInterstitial) {
            StarKnight.Companion.stopService()
            Utils.logDebugI(TAG, "33333333")
            startActivity(Intent(this@MainActivity, SkResultActivity::class.java))
        }
        sk.ad.preRequestAd(sk.ad.addTimeReward)
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
            addTimeDialog.show()
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
        checkNotificationPermission()
        openType = intent.getIntExtra(StarKnight.ExtraKey.OPEN_TYPE.key, -1)
        if (openType == StarKnight.ExtraValue.ADD_TIME_AND_CONNECT.value
            || openType == StarKnight.ExtraValue.IS_ADD_TIME_AND_CONNECT.value
        ) {
            if (BuildConfig.DEBUG) {
                Log.i("MainActivity", "come from buy user auto disconnect || manual back to app")
            }
            if (openType == StarKnight.ExtraValue.ADD_TIME_AND_CONNECT.value
                // 主动回到app，有剩余时间就不加时间，没有剩余时间就加上
                || (openType == StarKnight.ExtraValue.IS_ADD_TIME_AND_CONNECT.value && DataStore.remainTime <= 0)
            ) {
                addTime()
            }
            connect.launch(null)
        }

        mBinding.apply {
            adTimeTv.text = "+" + (sk.remoteConfig.remainTime / 60) + " mins"

        }
        handleAddTimeBtn()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openType = intent.getIntExtra(StarKnight.ExtraKey.OPEN_TYPE.key, -1)
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
                buyUserNotConnectedStateRemoveNativeAd()
            }

            BaseService.State.Connected -> {
                if (!FreqOperateLimit.doing(this, 500)) {
                    return
                }
                mBinding.stateDotView.setBackgroundResource(R.drawable.sk_dot_connected_state)
                SpanUtils.with(mBinding.stateTv)
                    .append(getString(R.string.sk_status))
                    .append(getString(R.string.sk_connected))
                    .setForegroundColor(getColor(R.color.sk_connected_state))
                    .create()
                mBinding.connectingLav.isVisible = false
                mBinding.noConnectingLl.isVisible = true
                mBinding.noConnectIv.setImageResource(R.drawable.sk_ic_connected)

                //预加载点击插屏
                sk.ad.preRequestAd(sk.ad.homeInterstitial)
                sk.ad.preRequestAd(sk.ad.resultInterstitial)

                // 普通用户连接成功后，移除广告
                if (!sk.user.isVip()) {
                    mBinding.nativeAdWrapper.isVisible = false
                }

                // 连接成功，仅买量用户展示插屏广告
                if (openType != 3) {//=3表示从点击vpn的通知进来，就不走连接成功结果页了，因为从通知进来还会走该状态

                    if (sk.user.isVip()) {
                        // 买量用户连接成功后，清除未连接的缓存广告
                        sk.ad.clearCacheAd(false)
                        sk.ad.preRequestAd(sk.ad.resultNative)
                        ad.requestLoadingAd(sk.ad.connectedInterstitial) {
                            Utils.logDebugI(TAG, "4444444")
                            startActivity(Intent(this@MainActivity, SkResultActivity::class.java))
                        }
                        // 预加载激励
                        sk.ad.preRequestAd(sk.ad.addTimeReward)

                    } else {
                        Utils.logDebugI(TAG, "55555555")
                        startActivity(Intent(this@MainActivity, SkResultActivity::class.java))
                    }

                    logConnect("sk_conn_succ_")
                }
            }

            else -> {
                if (!FreqOperateLimit.doing(lock, 200)) {
                    return
                }
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
                buyUserNotConnectedStateRemoveNativeAd()
                if (isCLickConnect) {
                    isCLickConnect = false
                    logConnect("sk_connect_fail_")
                }
                if (DataStore.serviceState == BaseService.State.Connecting) {
                    // 该种情况表示连接失败
                    Utils.logDebugI(TAG, "66666666")
                    startActivity(Intent(this@MainActivity, SkResultActivity::class.java).apply {
                        putExtra(SkResultActivity.KEY_CONNECT_FAIL, true)
                    })
                }
            }
        }
        DataStore.serviceState = state

        handleAddTimeBtn()
    }

    private fun buyUserNotConnectedStateRemoveNativeAd() {
        if (sk.user.isVip()) {
            mBinding.nativeAdWrapper.isVisible = false
        }
    }

     override fun serviceConnected(service: ISagerNetService) {
        Utils.logDebugI(TAG, "onServiceConnected")
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
    }

    override fun onCbSpeedUpdate(stats: SpeedDisplayData) {
        mBinding.downloadSpeedTv.text = Formatter.formatFileSize(this, stats.rxRateProxy)
        mBinding.uploadSpeedTv.text = Formatter.formatFileSize(this, stats.txRateProxy)
    }

    override fun addRemainTimeUpdateUi(time: Long) {
        val timeStr = Utils.formatMillis(time * 1000)
        mBinding.connectTimeTv.text = timeStr
    }

}
