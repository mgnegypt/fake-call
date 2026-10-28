package com.mgneg.fakecall.telecom

import android.annotation.SuppressLint
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

    // callCapablePhoneAccounts is permission-gated by the platform
    // (READ_PHONE_STATE), which the app deliberately does not declare:
    // this probe is best-effort diagnostics only — without the permission
    // it throws, runCatching turns that into null, and the caller falls
    // back to the getPhoneAccount probe. Call placement itself never
    // needed the permission.
    @SuppressLint("MissingPermission")
    fun isEnabled(context: Context): Boolean {
        val tm = context.getSystemService(TelecomManager::class.java) ?: return false
        val ours = handle(context)
        // Primary signal: the call-capable list. Accounts returned by
        // getCallCapablePhoneAccounts are by definition registered AND
        // user-enabled. The direct getPhoneAccount().isEnabled probe is
        // NOT trusted as the primary signal: on the owner's Nubia/REDMAGIC
        // stack it reported false while the account toggle was ON in
        // system settings and system-UI calls rang fine (field-verified
        // 2026-10-20) — the same probe that used to gate placement and
        // silently blocked every system-UI call.
        val capable = runCatching { tm.callCapablePhoneAccounts }.getOrNull()
        if (capable != null) {
            return capable.any {
                it.componentName == ours.componentName && it.id == ours.id
            }
        }
        return runCatching { tm.getPhoneAccount(ours)?.isEnabled == true }
            .getOrDefault(false)
    }

    /**
     * The honest tri-state verdict used by the Permissions UI. Runs the
     * same probes as [isEnabled] but keeps "could not read" distinct from
     * "disabled", and folds in stored connection [evidence] per
     * [AccountEnablementRules]. [isEnabled] stays for diagnostics, where
     * a plain observed boolean is what the log format records.
     */
    fun enablement(context: Context, evidence: Boolean): AccountEnablement {
        val tm = context.getSystemService(TelecomManager::class.java)
            ?: return if (evidence) AccountEnablement.ENABLED else AccountEnablement.UNKNOWN
        val ours = handle(context)
        @Suppress("MissingPermission")
        val capable = runCatching { tm.callCapablePhoneAccounts }.getOrNull()
        val capableContains = capable?.any {
            it.componentName == ours.componentName && it.id == ours.id
        }
        val direct = if (capable == null) {
            runCatching { tm.getPhoneAccount(ours) }.getOrNull()?.isEnabled
        } else {
            null
        }
        return AccountEnablementRules.decide(capableContains, direct, evidence)
    }

    fun accountSettingsIntent(): Intent = Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS)

    /**
     * Outcome of a placement attempt: success, or the refusal carried as a
     * typed error so callers can log and record exactly what the framework
     * said instead of collapsing every failure into a bare `false`.
     */
    sealed interface PlacementResult {
        data object Placed : PlacementResult
        data class Failed(val error: Throwable?) : PlacementResult
    }

    /**
     * Raise an incoming call through Telecom.
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
    ): PlacementResult {
        val tm = context.getSystemService(TelecomManager::class.java)
        if (tm == null) {
            android.util.Log.w(TAG, "placeIncomingCall: TelecomManager unavailable")
            return PlacementResult.Failed(null)
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
            PlacementResult.Placed
        }.onFailure {
            android.util.Log.w(TAG, "placeIncomingCall: addNewIncomingCall failed", it)
        }.getOrElse { PlacementResult.Failed(it) }
    }

    private const val TAG = "FakeCallTelecom"
}
