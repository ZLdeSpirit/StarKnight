package com.s.k.starknight.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.s.k.starknight.StarKnight
import com.s.k.starknight.databinding.SkActivitySelectServerBinding
import com.s.k.starknight.entity.LastConfig
import com.s.k.starknight.entity.ServerEntity
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import com.s.k.starknight.ui.adapter.SkSelectServerAdapter
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

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

    private var searchJob: Job? = null

    private var mAdapter: SkSelectServerAdapter? = null
    val initListData = sk.serverConfig.getServerConfig()


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
            searchEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    val query = editable.toString()
                    scheduleSearch(query)
                }
            })
        }
    }

    private fun scheduleSearch(query: String) {
        // 取消之前的搜索任务
        searchJob?.cancel()

        searchJob = lifecycleScope.launch {
            // 延迟500毫秒
            delay(500)

            if (query.isNotEmpty()) {
                performSearch(query)
            } else {
                clearSearchResults()
            }
        }
    }

    private suspend fun performSearch(query: String) {
        // 在IO线程执行搜索
        withContext(Dispatchers.IO) {
            val list = initListData.filter { it.countryParseName.contains(query,true) }
            // 切回主线程更新UI
            withContext(Dispatchers.Main) {
                if (list.isNotEmpty()){
                    mBinding.recyclerView.isVisible = true
                    mBinding.noContentTv.isVisible = false
                    val arrList = arrayListOf<ServerEntity>()
                    arrList.addAll(list)
                    setListData(arrList)
                }else{
                    // 显示空页面
                    mBinding.recyclerView.isVisible = false
                    mBinding.noContentTv.isVisible = true
                }
            }
        }
    }

    private fun clearSearchResults() {
        // 清空搜索结果
        mBinding.recyclerView.isVisible = true
        mBinding.noContentTv.isVisible = false
        val list = ArrayList<ServerEntity>()
        list.addAll(initListData)
        setListData(list)
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
//                                    StarKnight.reloadService()
                                    StarKnight.Companion.stopService()
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
//                    StarKnight.reloadService()
                    StarKnight.Companion.stopService()
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
        val list = ArrayList<ServerEntity>()
        list.addAll(initListData)
        mAdapter = SkSelectServerAdapter(list).apply {

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
            mBinding.recyclerView.adapter = this
        }

    }

    private fun setListData(list: ArrayList<ServerEntity>){
        mAdapter?.setData(list)
    }

}