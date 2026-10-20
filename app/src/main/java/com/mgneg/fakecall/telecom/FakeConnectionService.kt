package com.mgneg.fakecall.telecom

import android.content.Intent
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import com.mgneg.fakecall.FakeCallApp

class FakeConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        val extras = request.extras
        val historyId = extras.getLong(TelecomAccounts.EXTRA_HISTORY_ID, -1L)
        val audioUri = extras.getString(TelecomAccounts.EXTRA_AUDIO_URI)
        val name = extras.getString(TelecomAccounts.EXTRA_CALLER_NAME) ?: ""
        val number = request.address?.schemeSpecificPart ?: ""
        val connection = FakeConnection(applicationContext, name, number, audioUri, historyId)
        (applicationContext as FakeCallApp).callController.bindTelecomConnection(connection)
        return connection
    }

    /**
     * Self-managed calls are NOT guaranteed a system in-call UI: several OEM
     * dialers never render them. Telecom calls this hook when the incoming
     * call should become visible — we present our own call screen, which is
     * bridged to the live Connection through CallController.
     */
    override fun onShowIncomingCallUi(connection: Connection) {
        super.onShowIncomingCallUi(connection)
        startActivity(
            Intent(applicationContext, IncomingCallActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ) {
        (applicationContext as? FakeCallApp)?.callController
            ?.onTelecomStateChanged(CallState.Idle)
    }
}
