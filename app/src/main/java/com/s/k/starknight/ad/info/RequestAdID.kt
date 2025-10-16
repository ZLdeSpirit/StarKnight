package com.s.k.starknight.ad.info

import android.text.format.DateUtils
import com.s.k.starknight.ad.AdManager
import com.s.k.starknight.sk
import org.json.JSONObject

class RequestAdID(
    val id: String,
    val grade: Int,
    val adMold: AdManager.AdMold,
    val expireTime: Long = DateUtils.HOUR_IN_MILLIS
) {

    companion object {
        fun create(json: JSONObject, adMold: AdManager.AdMold): RequestAdID {
            val id = json.getString("sk_id")
            val grade = json.getInt("sk_grade")
            val adMold = sk.ad.stringToAdMold(json.optString("sk_mold")) ?: adMold
            return RequestAdID(id, grade, adMold)
        }
    }

}


