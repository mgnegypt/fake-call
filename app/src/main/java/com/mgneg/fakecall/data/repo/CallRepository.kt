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
        val entry = db.callHistoryDao().getById(historyId) ?: return
        if (entry.status != CallStatus.SCHEDULED) return
        controller.startIncoming(
            callerName = entry.callerName,
            callerNumber = entry.callerNumber,
            audioUri = entry.audioUri,
            historyId = entry.id
        )
    }

    suspend fun markFinished(historyId: Long, status: String, durationSec: Int) {
        updateStatus(historyId, status, durationSec)
    }

    /** Presentation started: SCHEDULED → RINGING. */
    suspend fun markRinging(historyId: Long) {
        val dao = db.callHistoryDao()
        val entry = dao.getById(historyId) ?: return
        if (entry.status == CallStatus.SCHEDULED) {
            dao.update(entry.copy(status = CallStatus.RINGING))
        }
    }

    /** The call was answered: RINGING → ANSWERED (duration comes at the end). */
    suspend fun markAnswered(historyId: Long) {
        val dao = db.callHistoryDao()
        val entry = dao.getById(historyId) ?: return
        if (entry.status == CallStatus.RINGING) {
            dao.update(entry.copy(status = CallStatus.ANSWERED))
        }
    }

    /** The call rang out unanswered: SCHEDULED/RINGING → MISSED. */
    suspend fun markMissed(historyId: Long) {
        transition(
            historyId, CallStatus.MISSED, 0,
            from = setOf(CallStatus.SCHEDULED, CallStatus.RINGING)
        )
    }

    /**
     * Guarded status write shared by the finish paths: a row may only be
     * finished while it is still in a live (non-terminal) status, so a
     * late or duplicate finish can never rewrite a settled row.
     */
    private suspend fun updateStatus(historyId: Long, status: String, durationSec: Int) {
        transition(
            historyId, status, durationSec,
            from = setOf(CallStatus.SCHEDULED, CallStatus.RINGING, CallStatus.ANSWERED)
        )
    }

    private suspend fun transition(
        historyId: Long,
        status: String,
        durationSec: Int,
        from: Set<String>
    ) {
        val dao = db.callHistoryDao()
        val entry = dao.getById(historyId) ?: return
        if (entry.status in from) {
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

    /**
     * Re-register alarms for still-pending scheduled calls (after reboot or
     * package replace). Rows whose time has clearly passed are settled as
     * MISSED instead of ringing late — see [ReschedulePolicy].
     */
    suspend fun reschedulePending() {
        val now = System.currentTimeMillis()
        db.callHistoryDao().pendingScheduled().forEach { entry ->
            when (ReschedulePolicy.decide(entry.triggerAt, now)) {
                ReschedulePolicy.Action.RESCHEDULE -> scheduler.schedule(entry.id, entry.triggerAt)
                ReschedulePolicy.Action.FIRE -> fireNow(entry.id)
                ReschedulePolicy.Action.MARK_MISSED -> markMissed(entry.id)
            }
        }
    }

    /**
     * App start: a fresh process has no live call, so any row still marked
     * RINGING belongs to a call that died with the previous process and is
     * settled as MISSED.
     */
    suspend fun sweepStaleRinging() {
        db.callHistoryDao().ringing().forEach { entry -> markMissed(entry.id) }
    }
}

/**
 * What to do with a SCHEDULED row when alarms are re-registered (boot,
 * package replace). Pure and unit-tested: the future is rescheduled, a
 * just-missed alarm (within the grace window — e.g. the device was
 * briefly off at the exact minute) still fires, and anything older is
 * settled as MISSED so a stale call never rings hours late.
 */
object ReschedulePolicy {
    enum class Action { RESCHEDULE, FIRE, MARK_MISSED }

    const val FIRE_GRACE_MS = 60_000L

    fun decide(triggerAt: Long, now: Long): Action = when {
        triggerAt > now -> Action.RESCHEDULE
        now - triggerAt <= FIRE_GRACE_MS -> Action.FIRE
        else -> Action.MARK_MISSED
    }
}
