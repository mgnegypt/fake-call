package com.mgneg.fakecall.telecom

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager

/**
 * A snapshot of one trigger attempt, recorded so the Logs screen
 * can show the device's own ground truth instead of guesses: what
 * mode was requested, what our enabled-probe saw, whether Telecom was
 * actually asked to place the call, what it answered (including the
 * exception when it refused), and how the registrar itself described the
 * accounts at that moment.
 */
data class CallAttemptDiagnostics(
    /** Epoch millis when the attempt was recorded. */
    val timestamp: Long = 0L,
    /** Settings call mode at trigger time ("telecom" | "app" | other). */
    val mode: String = "",
    /** Result of [TelecomAccounts.isEnabled] at trigger time. Diagnostic only — never gates routing. */
    val accountEnabled: Boolean = false,
    /** Whether placement via Telecom was attempted. */
    val placeAttempted: Boolean = false,
    /** Whether placement succeeded (only meaningful when [placeAttempted]). */
    val placeResult: Boolean = false,
    /** Exception class simple name when placement threw, else null. */
    val placeErrorClass: String? = null,
    /** Exception message when placement threw, else null. */
    val placeErrorMessage: String? = null,
    /** Final routing outcome (IncomingRoute name). */
    val route: String = "",
    /** Registrar snapshot lines (our account lookup + call-capable accounts). */
    val snapshot: List<String> = emptyList()
) {
    /**
     * One-line-per-field plain-text rendering (human-readable time first).
     * This is the single source of the field content: the Logs screen
     * displays these lines and [toLogText] exports them.
     */
    fun toDisplayLines(): List<String> {
        val lines = mutableListOf<String>()
        lines += "time=${formatTime(timestamp)}"
        lines += "mode=$mode"
        lines += "accountEnabled=$accountEnabled"
        lines += "placeAttempted=$placeAttempted"
        lines += "placeResult=$placeResult"
        placeErrorClass?.let {
            lines += "placeError=$it: ${placeErrorMessage.orEmpty()}"
        }
        lines += "route=$route"
        lines += snapshot
        return lines
    }

    /**
     * Full plain-text export of this record: an app header line and the
     * export time, then every display line. When no attempt was recorded
     * yet ([timestamp] == 0), the body is [noAttemptNote] instead, so the
     * exported file still explains itself.
     */
    fun toLogText(appName: String, noAttemptNote: String): String {
        val body = if (timestamp == 0L) noAttemptNote
            else toDisplayLines().joinToString("\n")
        return "$appName logs\nexported=${formatTime(System.currentTimeMillis())}\n\n$body\n"
    }

    private fun formatTime(epochMillis: Long): String =
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            .format(java.util.Date(epochMillis))
}

/**
 * Reads the registrar's own view of the phone accounts. All APIs are
 * guarded: OEM Telecom stacks vary, and a diagnostics read must never be
 * the thing that breaks a call trigger.
 */
object RegistrarSnapshot {

    // See TelecomAccounts.isEnabled: the callCapablePhoneAccounts probe
    // is best-effort diagnostics and intentionally permission-free.
    @SuppressLint("MissingPermission")
    fun capture(context: Context): List<String> {
        val tm = context.getSystemService(TelecomManager::class.java)
            ?: return listOf("registrar: TelecomManager unavailable")
        val lines = mutableListOf<String>()
        val ourHandle = TelecomAccounts.handle(context)
        val ours = runCatching { tm.getPhoneAccount(ourHandle) }.getOrNull()
        if (ours == null) {
            lines += "ourAccount: not found"
        } else {
            lines += "ourAccount: found enabled=${ours.isEnabled} " +
                "capabilities=${capabilitiesText(ours)}"
        }
        val capable = runCatching { tm.callCapablePhoneAccounts }.getOrNull()
        if (capable == null) {
            lines += "callCapable: unavailable"
        } else {
            lines += "callCapable: ${capable.size}"
            capable.forEach { handle ->
                val account = runCatching { tm.getPhoneAccount(handle) }.getOrNull()
                lines += "  - ${shortHandle(handle)} label=${account?.label} " +
                    "enabled=${account?.isEnabled}"
            }
        }
        return lines
    }

    private fun shortHandle(handle: PhoneAccountHandle): String {
        val cn = handle.componentName
        val cls = cn.className.substringAfterLast('.')
        return "${cn.packageName}/$cls#${handle.id}"
    }

    private fun capabilitiesText(account: PhoneAccount): String {
        val caps = mutableListOf<String>()
        val c = account.capabilities
        if (c and PhoneAccount.CAPABILITY_CALL_PROVIDER != 0) caps += "CALL_PROVIDER"
        if (c and PhoneAccount.CAPABILITY_SELF_MANAGED != 0) caps += "SELF_MANAGED"
        if (c and PhoneAccount.CAPABILITY_CONNECTION_MANAGER != 0) caps += "CONNECTION_MANAGER"
        if (c and PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION != 0) caps += "SIM_SUBSCRIPTION"
        if (caps.isEmpty()) caps += "0x${Integer.toHexString(c)}"
        return caps.joinToString("|")
    }
}
