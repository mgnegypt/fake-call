package com.mgneg.fakecall

import com.mgneg.fakecall.telecom.AccountEnablement
import com.mgneg.fakecall.telecom.AccountEnablementRules
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The phone-account tri-state (owner decision 2026-10-28, option C):
 * the real probe wins whenever it can be read, stored connection
 * evidence only fills the gap, and "unreadable" is honestly UNKNOWN —
 * never a fabricated DISABLED like the one the owner's Nubia showed
 * while its account toggle was ON.
 */
class AccountEnablementTest {

    @Test
    fun capableProbeContainingOursIsEnabled() {
        assertEquals(
            AccountEnablement.ENABLED,
            AccountEnablementRules.decide(capableContainsOurs = true, directEnabled = null, evidence = false)
        )
    }

    @Test
    fun capableProbeWithoutOursIsDisabled() {
        assertEquals(
            AccountEnablement.DISABLED,
            AccountEnablementRules.decide(capableContainsOurs = false, directEnabled = null, evidence = false)
        )
    }

    @Test
    fun capableProbeOverridesStoredEvidence() {
        // Owner condition: when the real probe works it beats evidence,
        // even evidence recorded earlier.
        assertEquals(
            AccountEnablement.DISABLED,
            AccountEnablementRules.decide(capableContainsOurs = false, directEnabled = null, evidence = true)
        )
        assertEquals(
            AccountEnablement.ENABLED,
            AccountEnablementRules.decide(capableContainsOurs = true, directEnabled = false, evidence = false)
        )
    }

    @Test
    fun directReadingUsedWhenCapableProbeUnreadable() {
        assertEquals(
            AccountEnablement.ENABLED,
            AccountEnablementRules.decide(capableContainsOurs = null, directEnabled = true, evidence = false)
        )
        assertEquals(
            AccountEnablement.DISABLED,
            AccountEnablementRules.decide(capableContainsOurs = null, directEnabled = false, evidence = true)
        )
    }

    @Test
    fun evidenceFillsTheGapWhenProbesAreSilent() {
        assertEquals(
            AccountEnablement.ENABLED,
            AccountEnablementRules.decide(capableContainsOurs = null, directEnabled = null, evidence = true)
        )
    }

    @Test
    fun noProbeNoEvidenceIsUnknownNeverDisabled() {
        assertEquals(
            AccountEnablement.UNKNOWN,
            AccountEnablementRules.decide(capableContainsOurs = null, directEnabled = null, evidence = false)
        )
    }

    @Test
    fun clearedEvidenceReturnsToUnknown() {
        // After a failed system call clears the stored evidence, the
        // verdict must fall back to UNKNOWN rather than sticking on
        // either ENABLED or DISABLED.
        assertEquals(
            AccountEnablement.UNKNOWN,
            AccountEnablementRules.decide(capableContainsOurs = null, directEnabled = null, evidence = false)
        )
    }
}
