package io.nekohasekai.sagernet.ktx

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.s.k.starknight.R

fun Context.launchCustomTab(link: String) {
    CustomTabsIntent.Builder().apply {
        setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
        setColorSchemeParams(
            CustomTabsIntent.COLOR_SCHEME_LIGHT,
            CustomTabColorSchemeParams.Builder().apply {
                setToolbarColor(getColor(R.color.sk_page_color))
            }.build()
        )
        setColorSchemeParams(
            CustomTabsIntent.COLOR_SCHEME_DARK,
            CustomTabColorSchemeParams.Builder().apply {
                setToolbarColor(getColor(R.color.sk_page_color))
            }.build()
        )
    }.build().apply {
        if (intent.resolveActivity(packageManager) != null) {
            launchUrl(this@launchCustomTab, Uri.parse(link))
        }
    }
}