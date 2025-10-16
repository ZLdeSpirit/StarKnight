package com.s.k.starknight.tools

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.s.k.starknight.R

fun TextView.setTitleGradientText(context: Context) {
    setGradientText(
        ContextCompat.getColor(context, R.color.sk_c_6EEBA6),
        ContextCompat.getColor(context, R.color.sk_c_29C8A2),
    )
}

fun TextView.setGradientText(vararg colors: Int) {
    val width = paint.measureText(text.toString())
    val gradient = LinearGradient(
        0f, 0f,
        width,
        0f,
        colors,
        null,
        Shader.TileMode.CLAMP
    )
    paint.shader = gradient
}