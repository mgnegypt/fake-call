package com.mgneg.fakecall.telecom

import android.content.Context
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

    private val _state = MutableStateFlow<CallState>(Idle)
    val state: StateFlow<CallState> = _state.asStateFlow()

    private val audioPlayer = CallAudioPlayer(context)
    private var ringtonePlayer: RingtonePlayer? = null

    fun startIncoming(callerName: String, callerNumber: String, audioUri: String?, historyId: Long) {
        app.appScope.launch {
            val mode = app.settings.callMode.first()
            val telecomOk = mode == "telecom" && TelecomAccounts.isEnabled(context) &&
                TelecomAccounts.placeIncomingCall(context, callerName, callerNumber, audioUri, historyId)
            if (telecomOk) {
                _state.value = CallState.Ringing(callerName, callerNumber, audioUri, historyId, CallPath.TELECOM)
            } else {
                _state.value = CallState.Ringing(callerName, callerNumber, audioUri, historyId, CallPath.APP_SCREEN)
                ringtonePlayer = RingtonePlayer(context).also { it.start() }
                IncomingCallNotifier.show(context, callerName, callerNumber)
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
}
