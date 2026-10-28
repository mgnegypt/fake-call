package com.mgneg.fakecall.telecom

import android.content.Context
import android.util.Log
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.data.db.CallStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The presentation path a live call is using. */
enum class CallPath { TELECOM, APP_SCREEN }

sealed interface CallState {
    data object Idle : CallState
    data class Ringing(
        val callerName: String,
        val callerNumber: String,
        val audioUri: String?,
        val historyId: Long,
        val path: CallPath
    ) : CallState

    data class InCall(
        val callerName: String,
        val callerNumber: String,
        val historyId: Long,
        val path: CallPath,
        val startedAt: Long = System.currentTimeMillis(),
        val muted: Boolean = false,
        val speaker: Boolean = false
    ) : CallState
}

/**
 * Owns the live-call state machine shared by the Telecom path and the app's
 * own call screen. History writes go through [CallRepository.markFinished].
 */
class CallController(private val context: Context) {

    private val app get() = context.applicationContext as FakeCallApp

    private val _state = MutableStateFlow<CallState>(CallState.Idle)
    val state: StateFlow<CallState> = _state.asStateFlow()

    private val audioPlayer = CallAudioPlayer(context)
    private var ringtonePlayer: RingtonePlayer? = null
    private var ringTimeoutJob: kotlinx.coroutines.Job? = null

    /**
     * Entry points for OUR call screen. That screen only ever serves the
     * app-screen mode: on the Telecom path the system phone app owns the
     * call UI and drives the connection's on* callbacks directly, so
     * there is deliberately no app-side control of Telecom calls here.
     */
    fun answerCurrent() = answerFromAppScreen()
    fun declineCurrent() = declineFromAppScreen()
    fun endCurrent() = endFromAppScreen()

    /**
     * Single funnel for every trigger (immediate, scheduled alarm, boot
     * reschedule, automation, preset, QS tile — all arrive here through
     * [com.mgneg.fakecall.data.repo.CallRepository.fireNow]).
     *
     * The two call modes are STRICT (see [CallRouting]):
     * - "app": our own call screen, exactly as before.
     * - "telecom": the system phone app ONLY, and placement is attempted
     *   UNCONDITIONALLY — the framework decides whether the account can
     *   take the call. An enabled-probe used to gate placement, and on at
     *   least one device it read false while the system settings showed
     *   the account ON, so Telecom was never even asked. The probe is
     *   still taken, but only as diagnostics. If placement fails,
     *   NOTHING rings and the app screen is NOT substituted — state
     *   returns to Idle, the history row is finished as FAILED, and the
     *   user gets an honest notification deep-linking to the system
     *   call-account settings.
     *
     * Every trigger leaves a logcat trail under tag "FakeCallTelecom"
     * (mode / accountEnabled / placeAttempted / placeResult /
     * placeError / route) AND persists a [CallAttemptDiagnostics] record
     * shown in the Settings "Last call attempt" card, so the device's
     * own report — not a guess — drives the next fix.
     */
    fun startIncoming(callerName: String, callerNumber: String, audioUri: String?, historyId: Long) {
        app.appScope.launch {
            // Overlap policy (documented in docs/CALL_ENGINE.md): one live
            // call at a time. A second trigger while a call is ringing or
            // in progress is settled as MISSED and never disturbs the
            // active call, its ringtone or its notification.
            val current = _state.value
            if (current is CallState.Ringing || current is CallState.InCall) {
                Log.i(TAG, "startIncoming historyId=$historyId settled MISSED: another call is active")
                if (historyId > 0) finishHistory(historyId, CallStatus.MISSED, 0)
                return@launch
            }
            val mode = app.settings.callMode.first()
            // Observed for diagnostics only; never gates routing.
            val accountEnabled = TelecomAccounts.isEnabled(context)
            val attempt = CallRouting.shouldAttemptPlacement(mode)
            var placeError: Throwable? = null
            val placed = if (attempt) {
                when (
                    val result = TelecomAccounts.placeIncomingCall(
                        context, callerName, callerNumber, audioUri, historyId
                    )
                ) {
                    TelecomAccounts.PlacementResult.Placed -> true
                    is TelecomAccounts.PlacementResult.Failed -> {
                        placeError = result.error
                        false
                    }
                }
            } else {
                false
            }
            val route = CallRouting.decide(mode, placed)
            Log.i(
                TAG,
                "startIncoming historyId=$historyId mode=$mode accountEnabled=$accountEnabled " +
                    "placeAttempted=$attempt placeResult=$placed " +
                    "placeError=${placeError?.let { "${it.javaClass.simpleName}: ${it.message}" } ?: "none"} " +
                    "route=$route"
            )
            runCatching {
                app.settings.saveLastCallAttempt(
                    CallAttemptDiagnostics(
                        timestamp = System.currentTimeMillis(),
                        mode = mode,
                        accountEnabled = accountEnabled,
                        placeAttempted = attempt,
                        placeResult = placed,
                        placeErrorClass = placeError?.javaClass?.simpleName,
                        placeErrorMessage = placeError?.message,
                        route = route.name,
                        snapshot = RegistrarSnapshot.capture(context)
                    )
                )
            }.onFailure { Log.w(TAG, "failed to persist call diagnostics", it) }
            // Presentation is starting: the history row leaves SCHEDULED
            // and becomes RINGING, so a later reschedule pass will never
            // re-fire a call that already rang.
            if (route != IncomingRoute.TELECOM_UNAVAILABLE && historyId > 0) {
                markRinging(historyId)
            }
            when (route) {
                IncomingRoute.TELECOM -> {
                    _state.value = CallState.Ringing(
                        callerName, callerNumber, audioUri, historyId, CallPath.TELECOM
                    )
                }
                IncomingRoute.APP_SCREEN -> {
                    _state.value = CallState.Ringing(
                        callerName, callerNumber, audioUri, historyId, CallPath.APP_SCREEN
                    )
                    val ringtoneUri = app.settings.ringtoneUri.first()
                    ringtonePlayer?.stop()
                    ringtonePlayer = RingtonePlayer(context, ringtoneUri).also { it.start() }
                    IncomingCallNotifier.show(context, callerName, callerNumber)
                    ringTimeoutJob?.cancel()
                    ringTimeoutJob = app.appScope.launch {
                        kotlinx.coroutines.delay(RING_TIMEOUT_MS)
                        val s = _state.value as? CallState.Ringing ?: return@launch
                        if (s.historyId != historyId || s.path != CallPath.APP_SCREEN) return@launch
                        ringtonePlayer?.stop(); ringtonePlayer = null
                        IncomingCallNotifier.dismiss(context)
                        _state.value = CallState.Idle
                        if (historyId > 0) finishHistory(historyId, CallStatus.MISSED, 0)
                    }
                }
                IncomingRoute.TELECOM_UNAVAILABLE -> {
                    Log.w(
                        TAG,
                        "Telecom mode requested but placement failed " +
                            "(accountEnabled=$accountEnabled placeResult=$placed " +
                            "placeError=${placeError?.let { "${it.javaClass.simpleName}: ${it.message}" } ?: "none"}); " +
                            "not substituting the app screen",
                        placeError
                    )
                    _state.value = CallState.Idle
                    // A system call just failed: any stored connection
                    // evidence is stale and must not keep claiming the
                    // account is enabled (owner condition 2).
                    app.appScope.launch {
                        runCatching { app.settings.setTelecomEvidence(false) }
                    }
                    IncomingCallNotifier.showTelecomUnavailable(context)
                    if (historyId > 0) finishHistory(historyId, CallStatus.FAILED, 0)
                }
            }
        }
    }

    /** Answer pressed on the app call screen. */
    fun answerFromAppScreen() {
        val s = _state.value as? CallState.Ringing ?: return
        if (s.path != CallPath.APP_SCREEN) return
        ringTimeoutJob?.cancel(); ringTimeoutJob = null
        ringtonePlayer?.stop(); ringtonePlayer = null
        IncomingCallNotifier.dismiss(context)
        _state.value = CallState.InCall(s.callerName, s.callerNumber, s.historyId, s.path)
        if (s.historyId > 0) markAnswered(s.historyId)
        s.audioUri?.let { audioPlayer.play(it) }
    }

    /** Decline pressed on the app call screen or its notification. */
    fun declineFromAppScreen() {
        val s = _state.value as? CallState.Ringing ?: return
        if (s.path != CallPath.APP_SCREEN) return
        ringTimeoutJob?.cancel(); ringTimeoutJob = null
        ringtonePlayer?.stop(); ringtonePlayer = null
        applySpeakerRoute(false)
        IncomingCallNotifier.dismiss(context)
        _state.value = CallState.Idle
        finishHistory(s.historyId, CallStatus.DECLINED, 0)
    }

    /** End pressed during a call on the app call screen. */
    fun endFromAppScreen() {
        val s = _state.value as? CallState.InCall ?: return
        if (s.path != CallPath.APP_SCREEN) return
        audioPlayer.stop()
        applySpeakerRoute(false)
        _state.value = CallState.Idle
        val duration = ((System.currentTimeMillis() - s.startedAt) / 1000).toInt()
        finishHistory(s.historyId, CallStatus.COMPLETED, duration)
    }

    fun setMuted(muted: Boolean) {
        val s = _state.value as? CallState.InCall ?: return
        _state.value = s.copy(muted = muted)
        // The toggle must silence the answer clip, not just flip the icon —
        // the same wiring the Telecom path has via onCallAudioStateChanged.
        audioPlayer.setMuted(muted)
    }

    fun setSpeaker(speaker: Boolean) {
        val s = _state.value as? CallState.InCall ?: return
        _state.value = s.copy(speaker = speaker)
        applySpeakerRoute(speaker)
    }

    /**
     * Route call audio to (or away from) the loudspeaker. On API 31+ the
     * deprecated isSpeakerphoneOn is replaced by the communication-device
     * API; turning off clears the device so the system picks the default
     * route again. Also used to reset the route when a call ends, so the
     * speaker choice never leaks into the next call or other apps.
     */
    private fun applySpeakerRoute(speaker: Boolean) {
        val am = context.getSystemService(android.media.AudioManager::class.java) ?: return
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            if (speaker) {
                val device = am.availableCommunicationDevices.firstOrNull {
                    it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                }
                if (device != null) am.setCommunicationDevice(device)
            } else {
                am.clearCommunicationDevice()
            }
        } else {
            @Suppress("DEPRECATION")
            am.isSpeakerphoneOn = speaker
        }
    }

    /** Events reported back by the Telecom connection (system UI path). */
    fun onTelecomStateChanged(state: CallState) {
        _state.value = state
    }

    fun finishHistory(historyId: Long, status: String, durationSec: Int) {
        app.appScope.launch { app.callRepository.markFinished(historyId, status, durationSec) }
    }

    fun markRinging(historyId: Long) {
        app.appScope.launch { app.callRepository.markRinging(historyId) }
    }

    fun markAnswered(historyId: Long) {
        app.appScope.launch { app.callRepository.markAnswered(historyId) }
    }

    companion object {
        /** Logcat tag for the Telecom routing trail; filter on this when debugging on-device. */
        const val TAG = "FakeCallTelecom"

        /** How long a call rings before it settles as MISSED (both paths). */
        const val RING_TIMEOUT_MS = 45_000L
    }
}
