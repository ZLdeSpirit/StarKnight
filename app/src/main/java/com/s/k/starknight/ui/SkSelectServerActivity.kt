package com.s.k.starknight.ui

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import com.s.k.starknight.StarKnight
import com.s.k.starknight.databinding.SkActivitySelectServerBinding
import com.s.k.starknight.entity.LastConfig
import com.s.k.starknight.entity.ServerEntity
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import com.s.k.starknight.ui.adapter.SkSelectServerAdapter
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.fmt.socks.SOCKSBean
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SkSelectServerActivity : BaseActivity() {
    companion object {
        const val KEY_NAME = "name"

        const val KEY_FLAG = "flag"
        const val KEY_CONFIG = "config"
    }

    private val TAG = "SkSelectServerActivity"
    private val mBinding by lazy { SkActivitySelectServerBinding.inflate(layoutInflater) }

    private var serverEntity: ServerEntity? = null

    val profileAccess = Mutex()
    val reloadAccess = Mutex()

    override fun isDisplayReturnAd(): Boolean {
        return true
    }

    override fun onRootView(): View {
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewInit()
        listenerInit()
    }

    private fun listenerInit() {
        mBinding.apply {
            backIv.setOnClickListener {
                onReturnActivity()
            }
        }
    }

    override fun onReturnActivity() {
        if (Utils.isConnectedState()) {
            ad.displayReturnAd {
                checkSaveFinish()
            }
        }else{
            checkSaveFinish()
        }
    }

    private fun checkSaveFinish(){
        if (serverEntity != null) {
            saveAndExit()
        }else{
            finish()
        }
    }

    fun saveAndExit() {
        runOnDefaultDispatcher {
            val proxyEntityList = ProfileManager.getAll()
            if (proxyEntityList.isNotEmpty()) {
                serverEntity!!.apply {
                    val serverEntitySocksBean = socksBeanList[0]
                    proxyEntityList.forEach {
                        val bean = it.socksBean
                        if (bean != null && bean.password == serverEntitySocksBean.password) {

                            var update: Boolean
                            var lastSelected: Long
                            profileAccess.withLock {
                                update = DataStore.selectedProxy != it.id
                                lastSelected = DataStore.selectedProxy
                                DataStore.selectedProxy = it.id
                            }

                            if (update) {
                                val lastConfig = LastConfig(countryParseName, countryCode, socksBeanList)
                                sk.preferences.setLastConfig(lastConfig)

                                ProfileManager.postUpdate(lastSelected)
                                if (DataStore.serviceState.canStop && reloadAccess.tryLock()) {
                                    StarKnight.reloadService()
                                    reloadAccess.unlock()
                                }
                            }
                            withContext(Dispatchers.Main){
                                setResult(200, Intent().apply {
                                    serverEntity!!.apply {
                                        putExtra(KEY_NAME, countryParseName)
                                        putExtra(KEY_FLAG, countryFlag)
                                        putParcelableArrayListExtra(KEY_CONFIG, socksBeanList)
                                    }
                                })
                                finish()
                            }
                            return@runOnDefaultDispatcher
                        }
                    }
                }

            }
            serverEntity!!.apply {
                val editingGroup = DataStore.editingGroup
                val lastConfig = LastConfig(countryParseName, countryCode, socksBeanList)
                sk.preferences.setLastConfig(lastConfig)
                val socksBean = socksBeanList[0]
                val proxyEntity = ProfileManager.createProfile(editingGroup, socksBean)
                profileAccess.withLock {
                    DataStore.selectedProxy = proxyEntity.id
                }
                if (DataStore.serviceState.canStop && reloadAccess.tryLock()) {
                    StarKnight.reloadService()
                    reloadAccess.unlock()
                }
            }
            withContext(Dispatchers.Main){
                setResult(200, Intent().apply {
                    serverEntity!!.apply {
                        putExtra(KEY_NAME, countryParseName)
                        putExtra(KEY_FLAG, countryFlag)
                        putParcelableArrayListExtra(KEY_CONFIG, socksBeanList)
                    }
                })
                finish()
            }
        }
    }

    private fun viewInit() {
        mBinding.apply {
            val list = sk.serverConfig.getServerConfig()
            recyclerView.adapter = SkSelectServerAdapter(list).apply {
                val lastConfig = sk.preferences.getLastConfig()
                if (lastConfig != null) {
                    initServerEntity(lastConfig.name, {
                        serverEntity = it
                    })
                }

                setSelectListener {
                    Log.d(TAG, "setSelectListener: $it")
                    serverEntity = it
                }
            }
        }
    }

}