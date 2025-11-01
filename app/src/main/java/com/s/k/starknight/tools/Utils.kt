package com.s.k.starknight.tools

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.R
import com.s.k.starknight.sk
import com.s.k.starknight.ui.BaseActivity
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore

object Utils {


    /**
     * 切换服务器不走结果也
     */
    var isSwitchServer = false

    var isChangeLanguage = false

    private val mActivityList = ArrayList<BaseActivity>()

    fun addActivity(activity: BaseActivity){
        if (!mActivityList.contains(activity)){
            mActivityList.add(activity)
        }
    }

    fun clearActivity(){
        if (mActivityList.isNotEmpty()) {
            mActivityList.clear()
        }
    }

    fun finishAllActivity(){
        if (mActivityList.isNotEmpty()) {
            mActivityList.forEach {
                it.finish()
            }
        }
    }

    fun logDebugI(tag: String, msg: String){
        if (BuildConfig.DEBUG){
            Log.i(tag,msg)
        }
    }

    fun getCountryFlag(countryCode: String): Int {
        return when (countryCode.lowercase()) {
            "us" -> {
                R.drawable.sk_ic_flag_us
            }

            "gb" -> {
                R.drawable.sk_ic_flag_gb
            }

            "ca" -> {
                R.drawable.sk_ic_flag_ca
            }

            "de" -> {
                R.drawable.sk_ic_flag_de
            }

            "nl" -> {
                R.drawable.sk_ic_flag_nl
            }

            "fr" -> {
                R.drawable.sk_ic_flag_fr
            }

            "ch" -> {
                R.drawable.sk_ic_flag_ch
            }

            "sg" -> {
                R.drawable.sk_ic_flag_sg
            }

            "jp" -> {
                R.drawable.sk_ic_flag_jp
            }

            "kr" -> {
                R.drawable.sk_ic_flag_kr
            }

            "au" -> {
                R.drawable.sk_ic_flag_au
            }

            "ru" -> {
                R.drawable.sk_ic_flag_ru
            }

            else -> {
                R.drawable.sk_ic_flag_us
            }
        }
    }

    fun getCountryName(countryCode: String): String {
        return when (countryCode.lowercase()) {
            "us" -> {
                "America"
            }

            "gb" -> {
                "England"
            }

            "ca" -> {
                "Canada"
            }

            "de" -> {
                "Germany"
            }

            "nl" -> {
                "Netherlands"
            }

            "fr" -> {
                "France"
            }

            "ch" -> {
                "Switzerland"
            }

            "sg" -> {
                "Singapore"
            }

            "jp" -> {
                "Japan"
            }

            "kr" -> {
                "Korea"
            }

            "au" -> {
                "Australia"
            }

            "ru" -> {
                "Russia"
            }

            else -> {
                "America"
            }
        }
    }

    fun isConnectedState() = DataStore.serviceState == BaseService.State.Connected

    @SuppressLint("DefaultLocale")
    fun formatMillis(duration: Long): String {
        val totalSeconds = duration / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours <= 0L) {
            String.format("%02d:%02d", minutes, seconds)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }


    /**
     * 通过包名打开Google Play
     * @param context 上下文
     * @param packageName 目标应用的包名
     */
    fun openGooglePlay(context: Context, packageName: String) {
        try {
            // 先尝试用Google Play应用打开
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                setPackage("com.android.vending") // 指定Google Play包名
            }

            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent)
            } else {
                // 如果没有Google Play应用，则用网页版
                openGooglePlayWeb(context, packageName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 发生异常时使用网页版
            openGooglePlayWeb(context, packageName)
        }
    }

    /**
     * 通过网页打开Google Play
     * @param context 上下文
     * @param packageName 目标应用的包名
     */
    fun openGooglePlayWeb(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, sk.getString(R.string.sk_error), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 检查Intent是否可用
     * @param context 上下文
     * @param intent 要检查的Intent
     * @return 是否可用
     */
    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        return try {
            val packageManager: PackageManager = context.packageManager
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查设备是否安装了Google Play商店
     * @param context 上下文
     * @return 是否已安装
     */
    fun isGooglePlayInstalled(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES) != null
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}