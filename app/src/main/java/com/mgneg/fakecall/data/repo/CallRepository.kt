package com.mgneg.fakecall.data.repo

import android.content.Context
import com.mgneg.fakecall.data.db.AppDatabase
import com.mgneg.fakecall.data.db.CallHistoryEntry
import com.mgneg.fakecall.data.db.CallStatus
import com.mgneg.fakecall.data.db.Preset
import com.mgneg.fakecall.data.settings.SettingsStore
import com.mgneg.fakecall.presets.PresetShortcuts
import com.mgneg.fakecall.scheduling.CallScheduler
import com.mgneg.fakecall.telecom.CallController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Single entry point used by the UI, the automation receiver, presets,
 * shortcuts and QS tiles to start or schedule a simulated call.
 */
class CallRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val settings: SettingsStore,
    private val scheduler: CallScheduler,
    private val controller: CallController
) {
    val history: Flow<List<CallHistoryEntry>> = db.callHistoryDao().observeAll()
    val presets: Flow<List<Preset>> = db.presetDao().observeAll()

    /**
     * Schedule (or immediately start) a call. Missing name/number fall back to
     * the saved defaults. Returns the history entry id.
     */
    suspend fun triggerCall(
        callerName: String?,
        callerNumber: String?,
        audioUri: String?,
        delaySec: Int
    ): Long {
        val name = callerName?.takeIf { it.isNotBlank() } ?: settings.defaultName.first()
        val number = callerNumber?.takeIf { it.isNotBlank() } ?: settings.defaultNumber.first()
        val triggerAt = System.currentTimeMillis() + delaySec.coerceAtLeast(0) * 1000L
        val id = db.callHistoryDao().insert(
            CallHistoryEntry(
                callerName = name,
                callerNumber = number,
                triggerAt = triggerAt,
                status = CallStatus.SCHEDULED,
                audioUri = audioUri
            )
        )
        if (delaySec <= 0) {
            fireNow(id)
        } else {
            scheduler.schedule(id, triggerAt)
        }
        return id
    }

    /** The alarm fired (or an immediate trigger): present the call now. */
    suspend fun fireNow(historyId: Long) {
        val entry = db.callHistoryDao().pendingScheduled().firstOrNull { it.id == historyId }
            ?: return
        controller.startIncoming(
            callerName = entry.callerName,
            callerNumber = entry.callerNumber,
            audioUri = entry.audioUri,
            historyId = entry.id
        )
    }

    suspend fun markFinished(historyId: Long, status: String, durationSec: Int) {
        val dao = db.callHistoryDao()
        val entry = dao.pendingScheduled().firstOrNull { it.id == historyId }
        if (entry != null) {
            dao.update(entry.copy(status = status, durationSec = durationSec))
        }
    }

    suspend fun clearHistory() = db.callHistoryDao().clearAll()

    suspend fun savePreset(preset: Preset): Boolean {
        val dao = db.presetDao()
        if (preset.id == 0L && dao.count() >= 5) return false
        dao.upsert(preset)
        PresetShortcuts.refresh(context, dao.observeAll().first())
        return true
    }

    suspend fun deletePreset(preset: Preset) {
        db.presetDao().delete(preset)
        PresetShortcuts.refresh(context, db.presetDao().observeAll().first())
    }

    /** Re-register alarms for still-pending scheduled calls (after reboot). */
    suspend fun reschedulePending() {
        val now = System.currentTimeMillis()
        db.callHistoryDao().pendingScheduled().forEach { entry ->
            if (entry.triggerAt > now) scheduler.schedule(entry.id, entry.triggerAt)
            else fireNow(entry.id)
        }
    }
}
