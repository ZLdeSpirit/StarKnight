package com.s.k.starknight.ad.loader

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.s.k.starknight.BuildConfig
import com.s.k.starknight.ad.AdManager
import com.s.k.starknight.ad.info.RequestAdInfo
import com.s.k.starknight.ad.info.SkAd
import com.s.k.starknight.sk
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow

class SkAdLoader(val adMold: AdManager.AdMold) {

    private val requestAd by lazy { RequestAd() }
    private val retry by lazy { RequestAdRetry() }
    private val saveAdList = mutableListOf<SkAd>()
    val adSize: Int
        get() {
            synchronized(saveAdList) {
                val iterator = saveAdList.iterator()
                while (iterator.hasNext()) {
                    if (!iterator.next().isValid) iterator.remove()
                }
                return saveAdList.size
            }
        }

    fun getAd(): SkAd? {
        synchronized(saveAdList) {
            val iterator = saveAdList.iterator()
            while (iterator.hasNext()) {
                val ad = iterator.next()
                if (ad.isValid) return ad
                iterator.remove()
            }
            return null
        }
    }

    private fun addAd(ad: SkAd) {
        synchronized(saveAdList) {
            saveAdList.add(ad)
            saveAdList.sortByDescending { it.adID.grade }
        }
    }

    fun preRequestAd(pos: String) {
        requestAd.requestAd(pos,null)
    }

    fun requestAd(pos: String, callback: () -> Unit) {
        val ad = getAd()
        if (ad != null) {
            if (BuildConfig.DEBUG) {
                Log.d("AdManager", "load: has cache ad type: ${adMold.adMold}")
            }
            callback.invoke()
            requestAd.requestAd(pos,null)
        } else {
            requestAd.requestAd(pos,callback)
        }
    }

    fun resetData() {
        requestAd.resetData()
    }

    fun clearCache(isClearConnectedAd: Boolean){
        val iterator = saveAdList.iterator()
        if (isClearConnectedAd){
            while (iterator.hasNext()){
                if (iterator.next().isConnected) iterator.remove()
            }
        }else{
            while (iterator.hasNext()){
                if (!iterator.next().isConnected) iterator.remove()
            }
        }
    }

    inner class RequestAdRetry {

        private var job: Job? = null
        private var failedCount = 0

        fun retry(pos: String,isSuccess: Boolean) {
            job?.cancel()
            job = null
            if (isSuccess) {
                failedCount = 0
            } else {
                failedCount++
                val time = ((2.0.pow(failedCount)) * DateUtils.SECOND_IN_MILLIS).toLong()
                if (time > 0) {
                    job = sk.scope.launch {
                        delay(time)
                        withContext(Dispatchers.Main) {
                            preRequestAd(pos)
                        }
                    }
                } else {
                    preRequestAd(pos)
                }
            }
        }
    }

    inner class RequestAd {

        private var requestAdInfo: RequestAdInfo? = null
        private var requestAdCallback: (() -> Unit)? = null
        private var requestingCount = 0

        fun requestAd(pos: String, callback: (() -> Unit)?) {
            val requestAdInfo = createRequestAdInfo()
            if (requestAdInfo == null) {
                if (BuildConfig.DEBUG) {
                    Log.d("AdManager", "load: config is null type: ${adMold.adMold}")
                }
                callback?.invoke()
                return
            }
            if (adSize + requestingCount >= requestAdInfo.count) {
                if (BuildConfig.DEBUG) {
                    Log.d("AdManager", "load: cache size is full type: ${adMold.adMold}")
                }
                callback?.let {
                    requestAdCallback?.invoke()
                    requestAdCallback = it
                }
                return
            }
            requestingCount++
            callback?.let {
                requestAdCallback?.invoke()
                requestAdCallback = it
            }
            sk.scope.launch {
                var quickAd: SkAd? = null
                for (adID in requestAdInfo.adIDList) {
                    val deferred = CompletableDeferred<SkAd?>()
                    withContext(Dispatchers.Main) {
                        when (adID.adMold) {
                            AdManager.AdMold.INTERSTITIAL -> {
                                sk.event.log("sk_req_int_$pos")
                                InterstitialAd.load(
                                    sk, adID.id,
                                    AdRequest.Builder().build(),
                                    object : InterstitialAdLoadCallback() {
                                        override fun onAdLoaded(p0: InterstitialAd) {
                                            super.onAdLoaded(p0)
                                            if (BuildConfig.DEBUG) {
                                                Log.d("AdManager", "load: load $pos InterstitialAd success")
                                            }
                                            sk.event.log("sk_req_int_succ_$pos")
                                            deferred.complete(SkAd(p0, this@SkAdLoader, adID))
                                        }

                                        override fun onAdFailedToLoad(p0: LoadAdError) {
                                            super.onAdFailedToLoad(p0)
                                            if (BuildConfig.DEBUG) {
                                                Log.d("AdManager", "load: load $pos InterstitialAd fail msg: ${p0.message}")
                                            }
                                            sk.event.log("sk_req_int_fail_$pos", Bundle().apply {
                                                putString("msg", p0.message)
                                            })
                                            deferred.complete(null)
                                        }
                                    })
                            }

                            AdManager.AdMold.NATIVE -> {
                                sk.event.log("sk_req_nat_$pos")
                                var ad: SkAd? = null
                                AdLoader.Builder(sk, adID.id).forNativeAd {
                                    if (BuildConfig.DEBUG) {
                                        Log.d("AdManager", "load: load $pos NativeAd success")
                                    }
                                    sk.event.log("sk_req_nat_succ_$pos")
                                    ad = SkAd(it, this@SkAdLoader, adID)
                                    deferred.complete(ad)
                                }.withAdListener(object : AdListener() {
                                    override fun onAdFailedToLoad(p0: LoadAdError) {
                                        super.onAdFailedToLoad(p0)
                                        if (BuildConfig.DEBUG) {
                                            Log.d("AdManager", "load: load $pos NativeAd fail msg: ${p0.message}")
                                        }
                                        sk.event.log("sk_req_nat_fail_$pos", Bundle().apply {
                                            putString("msg", p0.message)
                                        })
                                        deferred.complete(null)
                                    }

                                    override fun onAdClicked() {
                                        super.onAdClicked()
                                        ad?.clickCallback?.invoke()
                                    }
                                }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()
                                    .loadAd(AdRequest.Builder().build())
                            }

                            AdManager.AdMold.OPEN -> {
                                sk.event.log("sk_req_open_$pos")
                                AppOpenAd.load(
                                    sk, adID.id,
                                    AdRequest.Builder().build(),
                                    object : AppOpenAdLoadCallback() {
                                        override fun onAdLoaded(p0: AppOpenAd) {
                                            super.onAdLoaded(p0)
                                            if (BuildConfig.DEBUG) {
                                                Log.d("AdManager", "load: load $pos AppOpenAd success")
                                            }
                                            sk.event.log("sk_req_open_succ_$pos")
                                            deferred.complete(SkAd(p0, this@SkAdLoader, adID))
                                        }

                                        override fun onAdFailedToLoad(p0: LoadAdError) {
                                            super.onAdFailedToLoad(p0)
                                            if (BuildConfig.DEBUG) {
                                                Log.d("AdManager", "load: load $pos AppOpenAd fail msg: ${p0.message}")
                                            }
                                            sk.event.log("sk_req_open_fail_$pos", Bundle().apply {
                                                putString("msg", p0.message)
                                            })
                                            deferred.complete(null)
                                        }
                                    })
                            }

                            AdManager.AdMold.REWARDEDINTERSTITIAL -> {
                                sk.event.log("sk_req_rew_$pos")
                                RewardedInterstitialAd.load(
                                    sk,
                                    adID.id,
                                    AdRequest.Builder().build(),
                                    object : RewardedInterstitialAdLoadCallback() {
                                        override fun onAdLoaded(rewardedAd: RewardedInterstitialAd) {
                                            if (BuildConfig.DEBUG) {
                                                Log.d("AdManager", "load: load $pos RewardedInterstitialAd success")
                                            }
                                            sk.event.log("sk_req_rew_succ_$pos")
                                            deferred.complete(SkAd(rewardedAd, this@SkAdLoader, adID))
                                        }

                                        override fun onAdFailedToLoad(adError: LoadAdError) {
                                            if (BuildConfig.DEBUG) {
                                                Log.d("AdManager", "load: load $pos RewardedInterstitialAd fail msg: ${adError.message}")
                                            }
                                            sk.event.log("sk_req_rew_fail_$pos", Bundle().apply {
                                                putString("msg", adError.message)
                                            })
                                            deferred.complete(null)
                                        }
                                    },
                                )
                            }
                        }
                    }
                    quickAd = deferred.await()
                    if (quickAd != null) break
                }
                withContext(Dispatchers.Main) {
                    requestAdResult(pos, quickAd)
                }
            }
        }

        private fun requestAdResult(pos: String, quickAd: SkAd?) {
            quickAd?.let {
                addAd(it)
            }
            requestingCount--
            requestAdCallback?.invoke()
            requestAdCallback = null
            retry.retry(pos, quickAd != null)
        }

        private fun createRequestAdInfo(): RequestAdInfo? {
            if (requestAdInfo != null) return requestAdInfo
            requestAdInfo = RequestAdInfo.create(adMold)
            return requestAdInfo
        }

        fun resetData() {
            requestAdInfo = null
        }
    }

}