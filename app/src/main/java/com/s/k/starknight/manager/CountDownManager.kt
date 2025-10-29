package com.s.k.starknight.manager

import android.os.CountDownTimer
import com.s.k.starknight.StarKnight
import com.s.k.starknight.dialog.AddTimeDialog
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.database.DataStore

class CountDownManager {
    private var remainTime = 0L//s
    private val listenerList = ArrayList<(Long) -> Unit>()
    private var countDownTimeFinishListener : (() -> Unit)? = null
    private var timer: CountDownTimer? = null
    private val addTime by lazy { sk.remoteConfig.remainTime }

    fun init(){
        remainTime = sk.preferences.remainTime
    }

    fun getRemainTime(): Long{
        return remainTime
    }

    fun addRemainTime(){
        remainTime = remainTime + addTime
        sk.preferences.remainTime = remainTime
        listenerList.forEach {
            it.invoke(remainTime * 1000)
        }
        if (Utils.isConnectedState()){
            startCountDown(null)
        }
    }

    fun registerCountDownTime(listener: (Long) -> Unit){
        listener.invoke(remainTime * 1000)
        listenerList.add(listener)
    }

    fun unregisterCountDownTime(listener: (Long) -> Unit){
        if (listenerList.contains(listener)){
            listenerList.remove(listener)
        }
    }

    fun dispatch(time: Long){
        listenerList.forEach {
            it.invoke(time)
        }
    }

    fun clearListener(){
        listenerList.clear()
    }

    fun startCountDown(callback: ((Boolean) -> Unit)? = null){
        if (remainTime <= 0){
            callback?.invoke(false)
            return
        }
        callback?.invoke(true)
        stopCountDown()
        timer = object : CountDownTimer(remainTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainTime = remainTime - 1
                sk.preferences.remainTime = remainTime
                listenerList.forEach {
                    it.invoke(millisUntilFinished)
                }
                if (remainTime == 30L && sk.lifecycle.isAppVisible && sk.user.isVip()){
                    sk.lifecycle.getCurrentActivity().let {
                        if (it != null){
                            if (DataStore.serviceState.canStop){
                                StarKnight.Companion.stopService()
                                stopCountDown()
                            }
//                            AddTimeDialog(it, true).show()
                        }
                    }
                }
            }

            override fun onFinish() {
                countDownTimeFinishListener?.invoke()
            }
        }.apply {
            start()
        }

    }

    fun countTimeFinishListener(listener: () -> Unit){
        countDownTimeFinishListener = listener
    }

    fun stopCountDown(){
        timer?.cancel()
    }
}