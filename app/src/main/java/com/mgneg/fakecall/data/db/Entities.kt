package com.mgneg.fakecall.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object CallStatus {
    const val SCHEDULED = "SCHEDULED"
    const val ANSWERED = "ANSWERED"
    const val DECLINED = "DECLINED"
    const val COMPLETED = "COMPLETED"
}

@Entity(tableName = "call_history")
data class CallHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerName: String,
    val callerNumber: String,
    /** Epoch millis when the call rang (or is scheduled to ring). */
    val triggerAt: Long,
    /** One of [CallStatus]. */
    val status: String,
    val durationSec: Int = 0,
    val audioUri: String? = null
)

@Entity(
    tableName = "presets",
    indices = [Index(value = ["slot"], unique = true)]
)
data class Preset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 1..5 — also drives the launcher shortcut and QS tile number. */
    val slot: Int,
    val name: String,
    val callerName: String,
    val callerNumber: String,
    val audioUri: String? = null,
    val delaySec: Int = 0
)
