package com.s.k.starknight.ad.info

import android.os.SystemClock
import com.s.k.starknight.ad.loader.SkAdLoader

class SkAd(val ad: Any, val loader: SkAdLoader, val adID: RequestAdID) {

    private val createTime = SystemClock.elapsedRealtime()

    var isShow = true

    val isValid: Boolean
        get() {
            return isShow && SystemClock.elapsedRealtime() - createTime <= adID.expireTime
        }

    var clickCallback: (() -> Unit)? = null

}