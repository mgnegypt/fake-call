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
     * - "telecom": the system phone app ONLY. If the account is disabled
     *   or placement fails, NOTHING rings and the app screen is NOT
     *   substituted — state returns to Idle, the history row is finished
     *   as FAILED, and the user gets an honest notification deep-linking
     *   to the system call-account settings. Silently falling back used
     *   to mask a disabled account forever: the system UI was never
     *   reached and the user could not tell why.
     *
     * Every trigger leaves a logcat trail under tag "FakeCallTelecom"
     * (mode / accountEnabled / placeAttempted / placeResult / route) so a
     * device log capture can pinpoint which link failed.
     */
    fun startIncoming(callerName: String, callerNumber: String, audioUri: String?, historyId: Long) {
        app.appScope.launch {
            val mode = app.settings.callMode.first()
            val accountEnabled = TelecomAccounts.isEnabled(context)
            val attempt = CallRouting.shouldAttemptPlacement(mode, accountEnabled)
            val placed = attempt && TelecomAccounts.placeIncomingCall(
                context, callerName, callerNumber, audioUri, historyId
            )
            val route = CallRouting.decide(mode, accountEnabled, placed)
            Log.i(
                TAG,
                "startIncoming historyId=$historyId mode=$mode accountEnabled=$accountEnabled " +
                    "placeAttempted=$attempt placeResult=$placed route=$route"
            )
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
                    val vibrate = app.settings.vibrateOnRing.first()
                    ringtonePlayer = RingtonePlayer(context, ringtoneUri, vibrate).also { it.start() }
                    IncomingCallNotifier.show(context, callerName, callerNumber)
                }
                IncomingRoute.TELECOM_UNAVAILABLE -> {
                    Log.w(
                        TAG,
                        "Telecom mode requested but unavailable " +
                            "(accountEnabled=$accountEnabled placeResult=$placed); " +
                            "not substituting the app screen"
                    )
                    _state.value = CallState.Idle
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
        ringtonePlayer?.stop(); ringtonePlayer = null
        IncomingCallNotifier.dismiss(context)
        _state.value = CallState.InCall(s.callerName, s.callerNumber, s.historyId, s.path)
        s.audioUri?.let { audioPlayer.play(it) }
    }

    /** Decline pressed on the app call screen or its notification. */
    fun declineFromAppScreen() {
        val s = _state.value as? CallState.Ringing ?: return
        if (s.path != CallPath.APP_SCREEN) return
        ringtonePlayer?.stop(); ringtonePlayer = null
        IncomingCallNotifier.dismiss(context)
        _state.value = CallState.Idle
        finishHistory(s.historyId, CallStatus.DECLINED, 0)
    }

    /** End pressed during a call on the app call screen. */
    fun endFromAppScreen() {
        val s = _state.value as? CallState.InCall ?: return
        if (s.path != CallPath.APP_SCREEN) return
        audioPlayer.stop()
        _state.value = CallState.Idle
        val duration = ((System.currentTimeMillis() - s.startedAt) / 1000).toInt()
        finishHistory(s.historyId, CallStatus.COMPLETED, duration)
    }

    fun setMuted(muted: Boolean) {
        val s = _state.value as? CallState.InCall ?: return
        _state.value = s.copy(muted = muted)
    }

    @Suppress("DEPRECATION")
    fun setSpeaker(speaker: Boolean) {
        val s = _state.value as? CallState.InCall ?: return
        _state.value = s.copy(speaker = speaker)
        val am = context.getSystemService(android.media.AudioManager::class.java)
        am?.isSpeakerphoneOn = speaker
    }

    /** Events reported back by the Telecom connection (system UI path). */
    fun onTelecomStateChanged(state: CallState) {
        _state.value = state
    }

    fun finishHistory(historyId: Long, status: String, durationSec: Int) {
        app.appScope.launch { app.callRepository.markFinished(historyId, status, durationSec) }
    }

    companion object {
        /** Logcat tag for the Telecom routing trail; filter on this when debugging on-device. */
        const val TAG = "FakeCallTelecom"
    }
}
