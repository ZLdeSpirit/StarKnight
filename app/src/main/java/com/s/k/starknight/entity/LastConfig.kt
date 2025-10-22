package com.s.k.starknight.entity

import io.nekohasekai.sagernet.fmt.socks.SOCKSBean

data class LastConfig(
    val name : String,
    val countryCode : String,
    val config : ArrayList<SOCKSBean>
)