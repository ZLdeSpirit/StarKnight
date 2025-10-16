package com.s.k.starknight.ad.display

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.gms.ads.nativead.NativeAd
import com.s.k.starknight.databinding.SkNativeAdBuyBinding
import com.s.k.starknight.databinding.SkNativeAdNormalBinding
import com.s.k.starknight.sk

class NativeAdViewWrapper : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
    )

    private var nativeAd: NativeAd? = null


    fun displayAd(ad: NativeAd) {
        nativeAd?.destroy()
        nativeAd = ad
        if (sk.user.isVip()) {
            displayBg(ad)
        } else {
            displaySm(ad)
        }
    }

    private fun displaySm(nativeAd: NativeAd) {
        val binding = SkNativeAdNormalBinding.inflate(LayoutInflater.from(context))
        val adView = binding.root
        binding.run {
            adView.mediaView = mMediaView
            adView.callToActionView = actionBtn
            adView.iconView = iconIv
            adView.headlineView = titleTv
            adView.bodyView = descTv
        }
        adView.run {
            nativeAd.icon?.let {
                iconView?.isVisible = true
                (iconView as ImageView).setImageDrawable(it.drawable)
            }
            nativeAd.mediaContent?.let {
                mediaView?.mediaContent = it
            }
            (headlineView as TextView).apply {
                this.isVisible = !nativeAd.headline.isNullOrEmpty()
                this.text = nativeAd.headline
            }
            nativeAd.body?.let {
                bodyView?.isVisible = it.isNotEmpty()
                (bodyView as TextView).text = it
            }
            nativeAd.callToAction?.let {
                callToActionView?.isVisible = it.isNotEmpty()
                (callToActionView as TextView).text = it
            }
            setNativeAd(nativeAd)
        }
        removeAllViews()
        addView(adView)
    }

    private fun displayBg(nativeAd: NativeAd) {
        val binding = SkNativeAdBuyBinding.inflate(LayoutInflater.from(context))
        val adView = binding.root
        binding.run {
            adView.mediaView = mMediaView
            adView.callToActionView = actionBtn
            adView.iconView = iconIv
            adView.headlineView = titleTv
            adView.bodyView = descTv
        }
        adView.run {
            nativeAd.icon?.let {
                iconView?.isVisible = true
                (iconView as ImageView).setImageDrawable(it.drawable)
            }
            nativeAd.mediaContent?.let {
                mediaView?.mediaContent = it
            }
            (headlineView as TextView).apply {
                this.isVisible = !nativeAd.headline.isNullOrEmpty()
                this.text = nativeAd.headline
            }
            nativeAd.body?.let {
                bodyView?.isVisible = it.isNotEmpty()
                (bodyView as TextView).text = it
            }
            nativeAd.callToAction?.let {
                callToActionView?.isVisible = it.isNotEmpty()
                (callToActionView as TextView).text = it
            }
            setNativeAd(nativeAd)
        }
        removeAllViews()
        addView(adView)
        background = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        nativeAd?.destroy()
        nativeAd = null
    }
}