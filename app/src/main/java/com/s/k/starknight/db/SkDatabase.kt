package com.s.k.starknight.db


import androidx.room.Database
import androidx.room.RoomDatabase
import com.s.k.starknight.db.bean.SkAdValue

@Database(
    entities = [SkAdValue::class],
    version = 1,
    exportSchema = false
)
abstract class SkDatabase : RoomDatabase() {

    abstract fun skAdValueDao(): SkAdValueDao

}