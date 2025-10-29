package com.s.k.starknight.tools

import java.lang.ref.WeakReference
import java.util.LinkedList

object FreqOperateLimit {

    private val operateObjList: MutableList<WeakReference<*>> = LinkedList()
    private val timeHashMap = HashMap<WeakReference<*>, Long>()

    @JvmOverloads
    fun doing(obj: Any, minimumPeriod: Long): Boolean {
        var doing = false
        var lastOperateTime: Long = 0
        var wk: WeakReference<*>? = null
        val iterator = operateObjList.iterator()
        while (iterator.hasNext()) {
            val w = iterator.next()
            if (w.get() == null) {
                iterator.remove()
                timeHashMap.remove(w)
            } else if (w.get() === obj) {
                wk = w
            }
        }
        if (wk == null) {
            val cur = System.currentTimeMillis()
            wk = WeakReference(obj)
            operateObjList.add(wk)
            timeHashMap[wk] = cur
            doing = true
        } else {
            val cur = System.currentTimeMillis()
            lastOperateTime = timeHashMap[wk]!!
            if (cur - lastOperateTime > minimumPeriod) {
                doing = true
                lastOperateTime = cur
                timeHashMap[wk] = lastOperateTime
            }
        }
        return doing
    }
}