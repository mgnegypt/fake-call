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
    }

    private val controller get() = (appContext as FakeCallApp).callController

    override fun onAnswer() {
        if (answeredAt > 0) return
        answeredAt = System.currentTimeMillis()
        setActive()
        controller.onTelecomStateChanged(
            CallState.InCall(callerName, callerNumber, historyId, CallPath.TELECOM, answeredAt)
        )
        audioUri?.let { audioPlayer.play(it) }
    }

    override fun onReject() = finish(CallStatus.DECLINED)

    override fun onDisconnect() {
        val status = if (answeredAt > 0) CallStatus.COMPLETED else CallStatus.DECLINED
        finish(status)
    }

    override fun onAbort() = finish(CallStatus.DECLINED)

    private fun finish(status: String) {
        if (finished) return
        finished = true
        audioPlayer.stop()
        val duration = if (answeredAt > 0)
            ((System.currentTimeMillis() - answeredAt) / 1000).toInt() else 0
        runCatching {
            setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
            destroy()
        }
        controller.onTelecomStateChanged(CallState.Idle)
        if (historyId > 0) controller.finishHistory(historyId, status, duration)
    }
}
