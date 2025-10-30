package com.s.k.starknight.lifecycle

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.s.k.starknight.StarKnight
import com.s.k.starknight.sk
import com.s.k.starknight.tools.FreqOperateLimit
import com.s.k.starknight.tools.Utils
import com.s.k.starknight.ui.BaseActivity
import com.s.k.starknight.ui.SkSplashActivity
import io.nekohasekai.sagernet.Action

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

    override fun onActivityStarted(activity: Activity) {
        if (startCount++ == 0) {
            isAppVisible = true
            sendBroadcast(true)
            if (activity !is SkSplashActivity && !hasAdActivity) {
                if (!FreqOperateLimit.doing(this,500)){
                    return
                }
                if (Utils.isConnectedState()) {
                    activity.startActivity(Intent(activity, SkSplashActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(StarKnight.ExtraKey.OPEN_TYPE.key, 2)
                    })
                }else{
                    if (sk.user.isVip()) {
                        Utils.logDebugI("MainActivity", "onActivityStarted--------")
                        activity.startActivity(Intent(activity, SkSplashActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra(StarKnight.ExtraKey.OPEN_TYPE.key, StarKnight.ExtraValue.IS_ADD_TIME_AND_CONNECT.value)//自动连接vpn，没有时间增加时间，有时间不增加
                        })
                    }
                }
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (--startCount == 0) {
            isAppVisible = false
            sk.ad.preRequestAd(sk.ad.open)
            sendBroadcast(false)
        }
    }

    private fun sendBroadcast(isForeground: Boolean){
        if (sk.user.isVip()) {
            sk.sendBroadcast(Intent(Action.CHECK_FOREGROUND_OR_BACKGROUND).apply {
                setPackage(sk.packageName)
                putExtra(StarKnight.ExtraKey.IS_FOREGROUND.key, isForeground)
            })
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

    fun getCurrentActivity(): BaseActivity? {
        if (activityList.isEmpty()) return null
        val activity = activityList.last()
        if (activity is BaseActivity && activity.isVisibleActivity) return activity
        return null
    }
}