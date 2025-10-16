package com.s.k.starknight.lifecycle

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.s.k.starknight.StarKnight
import com.s.k.starknight.sk
import com.s.k.starknight.ui.BaseActivity
import com.s.k.starknight.ui.SkSplashActivity

class AppActivityLifecycle : Application.ActivityLifecycleCallbacks {

    private val activityList = mutableListOf<Activity>()
    private var startCount = 0
    var isAppVisible: Boolean = false
        private set

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        activityList.add(p0)
    }

    override fun onActivityDestroyed(p0: Activity) {
        activityList.remove(p0)
    }

    override fun onActivityPaused(p0: Activity) {

    }

    override fun onActivityResumed(p0: Activity) {

    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

    }

    override fun onActivityStarted(p0: Activity) {
        if (startCount++ == 0) {
            isAppVisible = true
            if (p0 !is SkSplashActivity && !hasAdActivity && sk.user.isVip()) {
                p0.startActivity(Intent(p0, SkSplashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(StarKnight.ExtraKey.OPEN_TYPE.key, 2)
                })
            }
        }
    }

    override fun onActivityStopped(p0: Activity) {
        if (--startCount == 0) {
            isAppVisible = false
            sk.ad.preRequestAd(sk.ad.open)
        }
    }

    private val hasAdActivity: Boolean
        get() {
            for (activity in activityList) {
                if (activity !is BaseActivity) {
                    return true
                }
            }
            return false
        }

}