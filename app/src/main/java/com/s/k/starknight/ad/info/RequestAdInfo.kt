package com.s.k.starknight.ad.info

import android.util.Base64
import com.s.k.starknight.ad.AdManager
import com.s.k.starknight.sk
import org.json.JSONObject

class RequestAdInfo(val count: Int, val adIDList: List<RequestAdID>) {

    companion object {
        fun create(adMold: AdManager.AdMold): RequestAdInfo? {
            val config = sk.remoteConfig.adMoldConfig
            if (config.isEmpty()) return null
            try {
                val json = JSONObject(
                    String(
                        Base64.decode(
                            config,
                            Base64.NO_WRAP
                        )
                    )
                ).optJSONObject(adMold.adMold)
                if (json == null) return null
                val count = json.getInt("sk_count")
                if (count <= 0) return null
                val adIDArray = json.getJSONArray("sk_id_array")
                if (adIDArray.length() <= 0) return null
                val adIDList = mutableListOf<RequestAdID>()
                for (i in 0 until adIDArray.length()) {
                    adIDList.add(RequestAdID.create(adIDArray.getJSONObject(i), adMold))
                }
                adIDList.sortByDescending { it.grade }
                return RequestAdInfo(count, adIDList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}