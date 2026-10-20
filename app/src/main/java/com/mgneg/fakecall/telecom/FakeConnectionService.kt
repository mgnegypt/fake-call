package com.mgneg.fakecall.telecom

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

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ) {
        (applicationContext as? FakeCallApp)?.callController
            ?.onTelecomStateChanged(CallState.Idle)
    }
}
