package com.s.k.starknight.manager

import com.s.k.starknight.dialog.AddTimeDialog
import com.s.k.starknight.tools.Utils
import com.s.k.starknight.ui.BaseActivity

object DialogDisplayManager {
    private const val MIN_SHOW_INTERVAL = 1000L
    private var lastShowTime = 0L
    private var currentDialog: AddTimeDialog? = null
    private val lock = Any()

    @Synchronized
    fun tryShowDialog(activity: BaseActivity, onClose: () -> Unit): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastShow = currentTime - lastShowTime

        if (timeSinceLastShow < MIN_SHOW_INTERVAL) {
            Utils.logDebugI("BaseActivity", "Too frequent, skip. Interval: ${timeSinceLastShow}ms")
            return false
        }

        if (currentDialog?.isShowing == true) {
            Utils.logDebugI("BaseActivity", "Dialog already showing")
            return false
        }

        lastShowTime = currentTime

        try {
            // 清除之前的对话框
            currentDialog?.dismiss()

            AddTimeDialog(activity).apply {
                currentDialog = this
                show()
                setCloseAddTime(true)
                setOnClickCloseListener {
                    onClose.invoke()
                    setCloseAddTime(false)
                }
                setOnDismissListener {
                    synchronized(lock) {
                        currentDialog = null
                    }
                }
                setOnCancelListener {
                    synchronized(lock) {
                        currentDialog = null
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Utils.logDebugI("BaseActivity", "Show dialog error: ${e.message}")
            synchronized(lock) {
                currentDialog = null
            }
            return false
        }
    }
}