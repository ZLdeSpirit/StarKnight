package com.s.k.starknight.manager

import android.util.Base64
import com.s.k.starknight.Constant
import com.s.k.starknight.entity.Account
import com.s.k.starknight.entity.LastConfig
import com.s.k.starknight.entity.ServerEntity
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.fmt.socks.SOCKSBean
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.get
import kotlin.random.Random

class ServerConfigManager {
    fun getServerConfig(): ArrayList<ServerEntity>{
        return newParseServerConfig()
    }

    /**
     * 原配置是前4尾和后4位进行替换
     * 取的时候将前4位和后4位替换
     */
    fun headTailReplace(originalString: String): String{
        val first4 = originalString.substring(0, 4)
        val middle = originalString.substring(4, originalString.length - 4)
        val last4 = originalString.substring(originalString.length - 4)

        return last4 + middle + first4
    }

    private fun newParseServerConfig(): ArrayList<ServerEntity> {
        val list = arrayListOf<ServerEntity>()
        try {
            val config = sk.remoteConfig.getNewServerConfig()
            val newConfig = headTailReplace(config)
            val jsonArray = JSONArray(String(Base64.decode(newConfig, Base64.NO_WRAP)))
            val length = jsonArray.length()
            if (length <= 0) return list
            parseJsonArray(list, jsonArray)
            list.sortByDescending { it.weight }
            return list

        } catch (e: Exception) {
            e.printStackTrace()
            return list
        }
    }

    private fun parseJsonArray(list: ArrayList<ServerEntity>, jsonArray: JSONArray){
        val length = jsonArray.length()
        for (i in 0..length - 1) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString(Constant.NAME)
            val code = jsonObject.getString(Constant.CODE)
            val weight = jsonObject.getInt(Constant.WEIGHT)
            val acc = jsonObject.getJSONArray(Constant.ACC)
            val accLength = acc.length()
            val accountList = arrayListOf<Account>()
            if (accLength > 0) {
                for (j in 0..accLength - 1) {
                    val accStr = acc.getString(j)
                    val accObj = JSONObject(accStr)
                    val host = accObj.getString(Constant.HOST)
                    val port = accObj.getString(Constant.PORT).toInt()
                    val pwd = accObj.getString(Constant.PWD)
                    val k = accObj.getInt(Constant.K)
                    val ac = accObj.getJSONArray(Constant.AC)
                    val acLength = ac.length()
                    val acArray = arrayListOf<String>()
                    if (acLength > 0) {
                        for (k in 0..acLength - 1) {
                            acArray.add(ac.getString(k))
                        }
                    }
                    val account = Account(host, port, pwd, k, acArray)
                    accountList.add(account)
                }
            }
            val serverEntity = ServerEntity(code, name, weight, accountList)
            list.add(serverEntity)
        }
    }

    fun setDefaultConfig() {
        val server = sk.preferences.getLastConfig()
        if (server != null) {
            val list = getServerConfig()
            var serverEntity = list.find { it.countryParseName == server.name }
            if (serverEntity != null) {
                setServerEntity(serverEntity)
            } else {
                if (list.isNotEmpty()) {
                    serverEntity = list[0]//已经排过序了，第一个就是权重最高的
                    setServerEntity(serverEntity)
                }
            }
            return
        }

        setDefaultNoCurrentServer()
    }

    fun setDefaultNoCurrentServer(){
        val list = getServerConfig()
        if (list.isNotEmpty()) {
            val serverEntity = list[0]//已经排过序了，第一个就是权重最高的
            setServerEntity(serverEntity)
        }
    }

    private fun setServerEntity(serverEntity: ServerEntity) {
        // 初始化editingId，默认就是0，实际上该值为插入数据库的id
        DataStore.editingId = 0
        DataStore.editingGroup = DataStore.selectedGroupForImport()
        serverEntity.apply {
            val editingGroup = DataStore.editingGroup
            val server = LastConfig(countryParseName, countryCode)
            sk.preferences.setLastConfig(server)
            val socksBean = generateSocksBean(this)
            sk.scope.launch {
                val proxyEntity = ProfileManager.createProfile(editingGroup, socksBean)
                DataStore.selectedProxy = proxyEntity.id
            }
        }
    }

    fun generateSocksBean(serverEntity: ServerEntity): SOCKSBean{
        val accList = serverEntity.acc
        val randomIndex = Random.nextInt(0, accList.size)
        val acc = accList[randomIndex]
        val socksBean = SOCKSBean()
        var username = generateRandomString(acc.k)
        val acList = acc.ac
        val randomIndex1 = Random.nextInt(0, acList.size)
        val ac = acList[randomIndex1]
        username = String.format(ac,username)
        socksBean.name = ""
        socksBean.serverAddress = acc.host
        socksBean.serverPort = acc.port
        socksBean.username = username
        socksBean.password = acc.pwd
        socksBean.protocol = 2
        socksBean.sUoT = false
        return socksBean
    }

    private fun generateRandomString(length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

}