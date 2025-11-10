package com.s.k.starknight.entity

class Account(
    val host: String,
    val port: Int,
    val pwd: String,
    val k: Int,//随机的长度
    val ac: ArrayList<String>
)