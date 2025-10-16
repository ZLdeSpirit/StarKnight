package com.s.k.starknight.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.s.k.starknight.db.bean.SkAdValue

@Dao
interface SkAdValueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(value: SkAdValue)

    @Query("SELECT * FROM sk_ad_value")
    fun queryAll(): List<SkAdValue>

    @Delete
    suspend fun delete(value: SkAdValue)
}