package com.s.k.starknight.manager

import android.util.Base64
import com.s.k.starknight.Constant
import com.s.k.starknight.entity.ServerEntity
import com.s.k.starknight.sk
import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.fmt.socks.SOCKSBean
import org.json.JSONObject

class ServerConfigManager {
    private lateinit var serverEntityList: ArrayList<ServerEntity>

    fun init(){
        serverEntityList = parseServerConfig()
    }

    fun getServerConfig(): ArrayList<ServerEntity>{
        return parseServerConfig()
    }

    fun removeHeadAndTail(originalString: String): String {
        val startLength = Constant.PARSE_CONFIG_LIST_START_FIELD.length
        val endLength = Constant.PARSE_CONFIG_LIST_END_FIELD.length

        return if (originalString.length >= startLength + endLength) {
            originalString.substring(startLength, originalString.length - endLength)
        } else {
            originalString // 或者根据需求返回空字符串或抛出异常
        }
    }

    private fun parseServerConfig(): ArrayList<ServerEntity> {
        val list = arrayListOf<ServerEntity>()
        try {
            val config = sk.remoteConfig.serverConfig
            val newConfig = removeHeadAndTail(config)
            Utils.logDebugI("ServerConfig", "config:$config")
            Utils.logDebugI("ServerConfig", "newConfig:$newConfig")
            val json = JSONObject(String(Base64.decode(newConfig, Base64.NO_WRAP)))
            val jsonArray = json.getJSONArray("show_list")
            val length = jsonArray.length()
            if (length <= 0) return list
            for (i in 0..length - 1) {
                val jsonObject = jsonArray.getJSONObject(i)
                val name = jsonObject.getString("name")
                val code = jsonObject.getString("code")
                val serverEntity = ServerEntity(code, name)
                val acc = jsonObject.getJSONArray("acc")
                val accLength = acc.length()
                if (accLength > 0) {
                    val socksBeanList = arrayListOf<SOCKSBean>()
                    for (j in 0..accLength - 1) {
                        //账号显示，一般是 host:port:account:password
                        val accStr = acc.getString(j)
                        val accArray = accStr.split(":")
                        val host = accArray[0]
                        val port = accArray[1].toInt()
                        val account = accArray[2]
                        val password = accArray[3]
                        val socksBean = SOCKSBean()
                        socksBean.name = ""
                        socksBean.serverAddress = host
                        socksBean.serverPort = port
                        socksBean.username = account
                        socksBean.password = password
                        socksBean.protocol = 2
                        socksBean.sUoT = false
                        socksBeanList.add(socksBean)
                    }
                    serverEntity.socksBeanList.addAll(socksBeanList)
                }
                list.add(serverEntity)
            }
            return list
        } catch (e: Exception) {
            return list
        }

    }
}