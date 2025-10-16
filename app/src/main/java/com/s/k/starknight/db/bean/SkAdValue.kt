package com.s.k.starknight.db.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sk_ad_value")
class SkAdValue(
    @ColumnInfo(name = "sk_unit")
    val unit: String,
    @ColumnInfo(name = "sk_value")
    val value: Long,
    @ColumnInfo(name = "sk_ad_id")
    val adId: String,
    @ColumnInfo(name = "sk_ad_type")
    val adType: Int,
    @ColumnInfo(name = "sk_source_name")
    val sourceName: String?,
    @ColumnInfo(name = "sk_save_time")
    val saveTime: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sk_value_id")
    @PrimaryKey(autoGenerate = true)
    val valueId: Long = 0
)