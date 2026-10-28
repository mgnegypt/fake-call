package com.mgneg.fakecall.telecom

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.mgneg.fakecall.FakeCallApp
import kotlinx.coroutines.launch

class FakeConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        // The bundle handed to addNewIncomingCall arrives NESTED under
        // EXTRA_INCOMING_CALL_EXTRAS inside the request extras — reading
        // the top level (our previous bug) loses name, audio and history id.
        val extras = request.extras?.getBundle(TelecomManager.EXTRA_INCOMING_CALL_EXTRAS)
        val historyId = extras?.getLong(TelecomAccounts.EXTRA_HISTORY_ID, -1L) ?: -1L
        val audioUri = extras?.getString(TelecomAccounts.EXTRA_AUDIO_URI)
        val name = extras?.getString(TelecomAccounts.EXTRA_CALLER_NAME).orEmpty()
        val number = request.address?.schemeSpecificPart.orEmpty()
        // Connection evidence (owner condition, 2026-10-28): the system
        // only delivers an incoming connection to an ENABLED account, so
        // reaching this callback is hard proof of enablement — unlike a
        // bare "placement returned", which some stacks report loosely.
        (applicationContext as? FakeCallApp)?.let { app ->
            app.appScope.launch { runCatching { app.settings.setTelecomEvidence(true) } }
        }
        return FakeConnection(applicationContext, name, number, audioUri, historyId)
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ) {
        // A system call failed to materialise: stored evidence no longer
        // reflects reality, so it is cleared (owner condition 2).
        (applicationContext as? FakeCallApp)?.let { app ->
            app.appScope.launch { runCatching { app.settings.setTelecomEvidence(false) } }
            app.callController?.onTelecomStateChanged(CallState.Idle)
        }
    }
}
