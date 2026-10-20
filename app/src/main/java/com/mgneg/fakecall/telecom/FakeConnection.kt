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
        // No PROPERTY_SELF_MANAGED: the account is a call provider, so the
        // system InCallService owns the UI for this connection.
        connectionCapabilities = CAPABILITY_MUTE or CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL
        setCallerDisplayName(callerName, android.telecom.TelecomManager.PRESENTATION_ALLOWED)
        setRinging()
    }

    private val controller get() = (appContext as FakeCallApp).callController

    override fun onAnswer() = doAnswer()

    override fun onReject() = finish(CallStatus.DECLINED)

    override fun onDisconnect() {
        val status = if (answeredAt > 0) CallStatus.COMPLETED else CallStatus.DECLINED
        finish(status)
    }

    /**
     * Safety net only: with a CALL_PROVIDER account the system InCallService
     * renders the call and Telecom normally never calls this. If an OEM
     * build still asks the app for UI, we present ours — its buttons are
     * bridged to this very connection.
     */
    override fun onShowIncomingCallUi() {
        appContext.startActivity(
            android.content.Intent(appContext, IncomingCallActivity::class.java)
                .addFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
        )
    }

    // Entry points for our own call screen (it controls this connection
    // directly; Connection exposes no public answer()/reject() API).
    fun answerFromUi() = doAnswer()
    fun rejectFromUi() = finish(CallStatus.DECLINED)
    fun endFromUi() {
        val status = if (answeredAt > 0) CallStatus.COMPLETED else CallStatus.DECLINED
        finish(status)
    }

    private fun doAnswer() {
        if (answeredAt > 0) return
        answeredAt = System.currentTimeMillis()
        setActive()
        controller.onTelecomStateChanged(
            CallState.InCall(callerName, callerNumber, historyId, CallPath.TELECOM, answeredAt)
        )
        audioUri?.let { audioPlayer.play(it) }
    }

    override fun onAbort() = finish(CallStatus.DECLINED)

    private fun finish(status: String) {
        if (finished) return
        finished = true
        controller.unbindTelecomConnection(this)
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
