package com.s.k.starknight.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import com.blankj.utilcode.util.SpanUtils
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.Constant
import com.s.k.starknight.ad.display.NativeAdViewWrapper
import com.s.k.starknight.databinding.SkActivitySettingsBinding
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils

class SkSettingsActivity : BaseActivity() {
    private val TAG = "SkSettingsActivity"

    private val mBinding by lazy { SkActivitySettingsBinding.inflate(layoutInflater) }

    override fun onDisplayNativeInfo(): Pair<String, NativeAdViewWrapper> {
        return sk.ad.settingsNative to mBinding.nativeAdWrapper
    }

    override fun needShowNative(): Boolean {
        return true
    }

    override fun isDisplayReturnAd(): Boolean {
        return true
    }
    override fun onRootView(): View {
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.apply {
            backIv.setOnClickListener {
                onReturnActivity()
            }

            languageLl.setOnClickListener {
                startActivity(Intent(this@SkSettingsActivity, SkLanguageActivity::class.java))
            }

            shareLl.setOnClickListener {
                Utils.openGooglePlay(this@SkSettingsActivity, packageName)
            }

            privacyLl.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.data = Constant.PRIVACY_URL.toUri()
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            appVersionTv.text = "V${BuildConfig.VERSION_NAME}"
        }
    }
}