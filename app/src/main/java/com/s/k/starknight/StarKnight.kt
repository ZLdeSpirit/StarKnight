package com.s.k.starknight

import android.app.Application
import com.s.k.starknight.manager.AppLanguage
import com.s.k.starknight.manager.AppPreferences
import com.s.k.starknight.manager.AppUserAttr

lateinit var sk: StarKnight
    private set
class StarKnight : Application(){

    val language by lazy { AppLanguage() }
    val preferences by lazy { AppPreferences() }

    val user by lazy { AppUserAttr() }

    override fun onCreate() {
        super.onCreate()
        sk = this
    }
}