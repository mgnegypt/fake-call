package com.mgneg.fakecall.telecom

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle

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
        return FakeConnection(applicationContext, name, number, audioUri, historyId)
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ) {
        // The controller's fallback covers failures at raise time; here the
        // platform itself refused, so just drop the ringing state.
        (applicationContext as? com.mgneg.fakecall.FakeCallApp)?.callController
            ?.onTelecomStateChanged(CallState.Idle)
    }
}
