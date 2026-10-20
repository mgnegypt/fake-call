package com.mgneg.fakecall.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM call_history ORDER BY triggerAt DESC")
    fun observeAll(): Flow<List<CallHistoryEntry>>

    @Query("SELECT * FROM call_history WHERE status = 'SCHEDULED'")
    suspend fun pendingScheduled(): List<CallHistoryEntry>

    @Insert
    suspend fun insert(entry: CallHistoryEntry): Long

    @Update
    suspend fun update(entry: CallHistoryEntry)

    @Query("DELETE FROM call_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM call_history")
    suspend fun clearAll()
}

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY slot ASC")
    fun observeAll(): Flow<List<Preset>>

    @Query("SELECT * FROM presets WHERE slot = :slot")
    suspend fun bySlot(slot: Int): Preset?

    @Query("SELECT COUNT(*) FROM presets")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preset: Preset): Long

    @Delete
    suspend fun delete(preset: Preset)
}
