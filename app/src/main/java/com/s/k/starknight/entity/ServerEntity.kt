package com.s.k.starknight.entity

import com.s.k.starknight.tools.Utils
import io.nekohasekai.sagernet.fmt.socks.SOCKSBean

class ServerEntity(
    val countryCode: String,
    val countryParseName: String,
    val weight: Int,
    val acc: ArrayList<Account>
){
    val countryFlag: Int
        get(){
            return Utils.getCountryFlag(countryCode)
        }
    val countryName : String
        get() {
            return Utils.getCountryName(countryCode)
        }
    var signalLevel: Int = 4
    var isSelected: Boolean = false

    val socksBeanList = arrayListOf<SOCKSBean>()

}