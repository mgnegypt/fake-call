package com.mgneg.fakecall.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CallHistoryEntry::class, Preset::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun presetDao(): PresetDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fake-call.db")
                // No destructive fallback: a future schema version must
                // ship a real Migration instead of wiping call history.
                .build()
    }
}
