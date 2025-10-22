package com.s.k.starknight.ad.display

import com.s.k.starknight.ui.BaseActivity


class DisplayConfig(val activity: BaseActivity) {

    var closeCallback: (() -> Unit)? = null
        private set

    var earnedRewardCallback: (() -> Unit)? = null
        private set

    var nativeAdView: NativeAdViewWrapper? = null
        private set

    fun setCloseCallback(callback: () -> Unit): DisplayConfig {
        closeCallback = callback
        return this
    }

    fun setNativeAdView(view: NativeAdViewWrapper): DisplayConfig {
        nativeAdView = view
        return this
    }

    fun setEarnedReward(callback: () -> Unit): DisplayConfig {
        earnedRewardCallback = callback
        return this
    }
}