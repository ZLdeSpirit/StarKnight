package com.s.k.starknight.entity

import androidx.annotation.DrawableRes

data class SelectEntity(
    @DrawableRes
    val countryFlag: Int,
    val countryName: String,
    var signalLevel: Int,
    var isSelected: Boolean = false,
)