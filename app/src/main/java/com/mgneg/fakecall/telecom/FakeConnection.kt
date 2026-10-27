package com.mgneg.fakecall.telecom

import android.content.Context
import android.telecom.Connection
import android.telecom.DisconnectCause
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.data.db.CallStatus

/** A Telecom connection on the call-provider account: rings in the system phone app, then plays audio when answered. */
class FakeConnection(
    context: Context,
    private val callerName: String,
    private val callerNumber: String,
    private val audioUri: String?,
    private val historyId: Long
) : Connection() {

    private val appContext = context.applicationContext
    private val audioPlayer = CallAudioPlayer(appContext)
    private var answeredAt: Long = 0L
    private var finished = false

    private val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val ringTimeout = Runnable {
        if (!finished && answeredAt == 0L) {
            finish(CallStatus.MISSED, DisconnectCause(DisconnectCause.MISSED))
        }
    }

    init {
        // Construction aligned with how call-provider connections are
        // expected to present themselves to the system InCallService:
        // a real tel: address and display name on the connection itself
        // (without these the system UI shows "Unknown"), VoIP audio mode,
        // mute capability, then initializing -> ringing.
        val address = android.net.Uri.fromParts(
            android.telecom.PhoneAccount.SCHEME_TEL, callerNumber, null
        )
        setAddress(address, android.telecom.TelecomManager.PRESENTATION_ALLOWED)
        setCallerDisplayName(
            callerName.ifBlank { callerNumber },
            android.telecom.TelecomManager.PRESENTATION_ALLOWED
        )
        connectionCapabilities = CAPABILITY_MUTE
        setAudioModeIsVoip(true)
        setInitializing()
        setRinging()
        timeoutHandler.postDelayed(ringTimeout, CallController.RING_TIMEOUT_MS)
    }

    private val controller get() = (appContext as FakeCallApp).callController

    /**
     * Some Telecom stacks deliver the answer with an explicit video
     * state instead of calling the no-arg variant; route both to the
     * same handler so answering works identically everywhere.
     */
    override fun onAnswer(videoState: Int) = onAnswer()

    override fun onAnswer() {
        if (answeredAt > 0) return
        timeoutHandler.removeCallbacks(ringTimeout)
        answeredAt = System.currentTimeMillis()
        setActive()
        controller.onTelecomStateChanged(
            CallState.InCall(callerName, callerNumber, historyId, CallPath.TELECOM, answeredAt)
        )
        if (historyId > 0) controller.markAnswered(historyId)
        audioUri?.let { audioPlayer.play(it) }
    }

    /**
     * We advertise CAPABILITY_MUTE, so honor it: when the user mutes
     * from the system call UI, silence the answer clip (and restore it
     * on unmute) instead of leaving it playing at full volume.
     */
    override fun onCallAudioStateChanged(state: android.telecom.CallAudioState) {
        super.onCallAudioStateChanged(state)
        audioPlayer.setMuted(state.isMuted)
    }

    override fun onReject() = finish(CallStatus.DECLINED)

    override fun onDisconnect() {
        val status = if (answeredAt > 0) CallStatus.COMPLETED else CallStatus.DECLINED
        finish(status)
    }

    override fun onAbort() = finish(CallStatus.DECLINED)

    private fun finish(status: String, cause: DisconnectCause = DisconnectCause(DisconnectCause.LOCAL)) {
        if (finished) return
        finished = true
        timeoutHandler.removeCallbacks(ringTimeout)
        audioPlayer.stop()
        val duration = if (answeredAt > 0)
            ((System.currentTimeMillis() - answeredAt) / 1000).toInt() else 0
        runCatching {
            setDisconnected(cause)
            destroy()
        }
        controller.onTelecomStateChanged(CallState.Idle)
        if (historyId > 0) controller.finishHistory(historyId, status, duration)
    }
}
