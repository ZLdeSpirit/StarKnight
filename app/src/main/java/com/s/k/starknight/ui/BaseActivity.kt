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
import com.s.k.starknight.ad.display.DisplayConfig
import com.s.k.starknight.ad.display.NativeAdViewWrapper
import com.s.k.starknight.ad.pos.AdPos
import com.s.k.starknight.manager.AppLanguage
import com.s.k.starknight.sk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseActivity : AppCompatActivity(), AppLanguage.OnLanguageChangeCallback{
    val ad by lazy { ActivityAd() }

    var isVisibleActivity: Boolean = true
        private set

    abstract fun onRootView(): View

    override fun onCreate(savedInstanceState: Bundle?) {
        sk.language.setContextLanguage(this)
        super.onCreate(savedInstanceState)
        adapterScreen()
        setContentView(onRootView())
        addReturnCallback()
        sk.language.addLanguageChangeCallback(this)
        lifecycle.addObserver(ad)
    }

    override fun onResume() {
        isVisibleActivity = true
        super.onResume()
    }

    override fun onPause() {
        isVisibleActivity = false
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
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
        ad.displayReturnAd {
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
                sk.ad.preRequestAd(sk.ad.returnInterstitial)
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
                it.displayAd(DisplayConfig(this@BaseActivity).setNativeAdView(adView))
            }
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
                displayAd(sk.ad.returnInterstitial, true, callback)
                return
            }
            callback.invoke()
        }

    }
}