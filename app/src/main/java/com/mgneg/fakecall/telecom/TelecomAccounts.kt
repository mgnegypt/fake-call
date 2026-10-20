package com.mgneg.fakecall.telecom

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager

/**
 * Registration and use of the Telecom PhoneAccount.
 *
 * The account is registered as a CALL_PROVIDER (not self-managed). That is
 * the mechanism that makes the SYSTEM phone app render the incoming-call
 * UI, play the ringtone, and write the call log entry: non-self-managed
 * calls are handled by the default dialer's InCallService. A self-managed
 * account, by contrast, is responsible for its own UI and many OEM dialers
 * never show one for it — the call effectively rings invisibly.
 */
object TelecomAccounts {

    const val ACCOUNT_ID = "fake_call_account"
    const val EXTRA_HISTORY_ID = "extra_history_id"
    const val EXTRA_AUDIO_URI = "extra_audio_uri"
    const val EXTRA_CALLER_NAME = "extra_caller_name"

    fun handle(context: Context): PhoneAccountHandle =
        PhoneAccountHandle(
            ComponentName(context, FakeConnectionService::class.java),
            ACCOUNT_ID
        )

    fun register(context: Context, label: String) {
        val tm = context.getSystemService(TelecomManager::class.java) ?: return
        val account = PhoneAccount.builder(handle(context), label)
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .setSupportedUriSchemes(listOf(PhoneAccount.SCHEME_TEL))
            .setShortDescription(label)
            .build()
        runCatching { tm.registerPhoneAccount(account) }
    }

    fun isEnabled(context: Context): Boolean {
        val tm = context.getSystemService(TelecomManager::class.java) ?: return false
        return runCatching { tm.getPhoneAccount(handle(context))?.isEnabled == true }
            .getOrDefault(false)
    }

    fun accountSettingsIntent(): Intent = Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS)

    /**
     * Raise an incoming call through Telecom. Returns false if it could not
     * be placed.
     *
     * Extras contract (must mirror [FakeConnectionService]): the address
     * sits at the TOP level under [TelecomManager.EXTRA_INCOMING_CALL_ADDRESS],
     * while every app-specific value travels NESTED inside a child bundle
     * under [TelecomManager.EXTRA_INCOMING_CALL_EXTRAS]. Telecom delivers
     * the outer bundle as `ConnectionRequest.extras` and the service reads
     * the nested child — values placed at the top level are silently lost
     * on that side (name, audio and history id all arrived empty).
     */
    fun placeIncomingCall(
        context: Context,
        callerName: String,
        callerNumber: String,
        audioUri: String?,
        historyId: Long
    ): Boolean {
        val tm = context.getSystemService(TelecomManager::class.java)
        if (tm == null) {
            android.util.Log.w(TAG, "placeIncomingCall: TelecomManager unavailable")
            return false
        }
        val appExtras = Bundle().apply {
            putLong(EXTRA_HISTORY_ID, historyId)
            putString(EXTRA_CALLER_NAME, callerName)
            audioUri?.let { putString(EXTRA_AUDIO_URI, it) }
        }
        val extras = Bundle().apply {
            putParcelable(
                TelecomManager.EXTRA_INCOMING_CALL_ADDRESS,
                Uri.fromParts(PhoneAccount.SCHEME_TEL, callerNumber, null)
            )
            putBundle(TelecomManager.EXTRA_INCOMING_CALL_EXTRAS, appExtras)
        }
        return runCatching {
            tm.addNewIncomingCall(handle(context), extras)
            true
        }.onFailure {
            android.util.Log.w(TAG, "placeIncomingCall: addNewIncomingCall failed", it)
        }.getOrDefault(false)
    }

    private const val TAG = "FakeCallTelecom"
}
