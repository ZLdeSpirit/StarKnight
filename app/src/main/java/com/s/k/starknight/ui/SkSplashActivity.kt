package com.s.k.starknight.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.s.k.starknight.StarKnight
import com.s.k.starknight.ad.pos.AdPos
import com.s.k.starknight.databinding.SkActivitySplashBinding
import com.s.k.starknight.sk

class SkSplashActivity : BaseActivity() {

    private val mBinding by lazy { SkActivitySplashBinding.inflate(layoutInflater) }

    private var openType = -1
    private val handler = Handler(Looper.getMainLooper())
    private val timeOutRunnable = Runnable {
        displayAd(null)
    }
    private var isDisplayAd = true

    override fun onRootView(): View {
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            val parseUrl = intent.getStringExtra(StarKnight.ExtraKey.PARSE_URL.key)
//            sk.notify.cleanNotification(intent.getIntExtra(StarKnight.ExtraKey.NOTIFY_ID.key, -1))
//            if (!parseUrl.isNullOrEmpty()) {
//                sk.scanVideo.parseVideo(parseUrl)
//            }
        }
        sk.event.log("sk_op_open", Bundle().apply {
            putString(
                "type", when (openType) {
                    1 -> "msg"
                    2 -> "hot"
                    else -> "oth"
                }
            )
        })
    }

    private fun requestAd() {
        handler.postDelayed(timeOutRunnable, 16300)
        ad.requestAd(onFullScreenDisplayPos()) {
            handler.removeCallbacks(timeOutRunnable)
            displayAd(it)
        }
        ad.preRequestAd()
    }

    private fun displayAd(adPos: AdPos?) {
        if (!isDisplayAd) return
        isDisplayAd = false
        if (adPos != null) {
            ad.displayAd(adPos, false) {
                skipActivity()
            }
        } else {
            ad.displayAd(onFullScreenDisplayPos(), false) {
                skipActivity()
            }
        }
    }

    private fun skipActivity() {
        if (isDestroyed) return
        if (openType == 2 || (!isVisibleActivity && !sk.lifecycle.isAppVisible)) {
            finish()
            return
        }
        startActivity(
            Intent(
                this,
                if (sk.preferences.isSetAppLanguage) MainActivity::class.java else SkLanguageActivity::class.java
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
            listOf(sk.ad.homeNative, sk.ad.openInterstitial)
        } else {
            listOf(sk.ad.languageNative, sk.ad.languageInterstitial)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeOutRunnable)
    }

    private fun requestConsentInfoUpdate() {
        if (!sk.isRequestUmp) {
            requestAd()
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
                        sk.initAd()
                    }
                    requestAd()
                }
            },
            {
                sk.isRequestUmp = false
                requestAd()
            })
    }
}