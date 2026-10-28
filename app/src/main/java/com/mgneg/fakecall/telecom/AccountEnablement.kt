package com.mgneg.fakecall.telecom

/** Tri-state answer to "is our phone account enabled?". */
enum class AccountEnablement { ENABLED, DISABLED, UNKNOWN }

/**
 * Pure priority rules for the account-enablement verdict (owner decision
 * 2026-10-28, option C). The platform probes are unreliable in layers:
 * the call-capable list is the only authoritative one but is gated by
 * READ_PHONE_STATE (not declared since 1.3), and some OEM stacks return
 * no account at all from getPhoneAccount even when the account is on.
 *
 * Priority:
 * 1. [capableContainsOurs] non-null (the real probe could be read —
 *    permission granted): authoritative, overrides everything, including
 *    stored evidence.
 * 2. [directEnabled] non-null (the OEM returned our account): its flag.
 * 3. Stored connection evidence (FakeConnectionService actually created
 *    an incoming connection): ENABLED.
 * 4. Otherwise UNKNOWN — never a fabricated DISABLED.
 */
object AccountEnablementRules {
    fun decide(
        capableContainsOurs: Boolean?,
        directEnabled: Boolean?,
        evidence: Boolean
    ): AccountEnablement = when {
        capableContainsOurs != null ->
            if (capableContainsOurs) AccountEnablement.ENABLED else AccountEnablement.DISABLED
        directEnabled != null ->
            if (directEnabled) AccountEnablement.ENABLED else AccountEnablement.DISABLED
        evidence -> AccountEnablement.ENABLED
        else -> AccountEnablement.UNKNOWN
    }
}
