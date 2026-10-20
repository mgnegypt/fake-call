package com.mgneg.fakecall.telecom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallRoutingTest {

    @Test
    fun `app mode always uses the app screen regardless of account or placement`() {
        assertEquals(
            IncomingRoute.APP_SCREEN,
            CallRouting.decide(CallRouting.MODE_APP, accountEnabled = false, placed = false)
        )
        assertEquals(
            IncomingRoute.APP_SCREEN,
            CallRouting.decide(CallRouting.MODE_APP, accountEnabled = true, placed = true)
        )
        // Unknown / legacy values behave like the app screen, never Telecom.
        assertEquals(
            IncomingRoute.APP_SCREEN,
            CallRouting.decide("something-else", accountEnabled = true, placed = true)
        )
    }

    @Test
    fun `telecom mode with enabled account and successful placement uses telecom`() {
        assertEquals(
            IncomingRoute.TELECOM,
            CallRouting.decide(CallRouting.MODE_TELECOM, accountEnabled = true, placed = true)
        )
    }

    @Test
    fun `telecom mode with disabled account is unavailable and never falls back`() {
        assertEquals(
            IncomingRoute.TELECOM_UNAVAILABLE,
            CallRouting.decide(CallRouting.MODE_TELECOM, accountEnabled = false, placed = false)
        )
        // Even a (hypothetical) successful placement must not rescue a
        // disabled account: placement is not attempted in that state.
        assertEquals(
            IncomingRoute.TELECOM_UNAVAILABLE,
            CallRouting.decide(CallRouting.MODE_TELECOM, accountEnabled = false, placed = true)
        )
    }

    @Test
    fun `telecom mode with failed placement is unavailable and never falls back`() {
        assertEquals(
            IncomingRoute.TELECOM_UNAVAILABLE,
            CallRouting.decide(CallRouting.MODE_TELECOM, accountEnabled = true, placed = false)
        )
    }

    @Test
    fun `placement is attempted only for telecom mode with an enabled account`() {
        assertTrue(CallRouting.shouldAttemptPlacement(CallRouting.MODE_TELECOM, accountEnabled = true))
        assertFalse(CallRouting.shouldAttemptPlacement(CallRouting.MODE_TELECOM, accountEnabled = false))
        assertFalse(CallRouting.shouldAttemptPlacement(CallRouting.MODE_APP, accountEnabled = true))
        assertFalse(CallRouting.shouldAttemptPlacement(CallRouting.MODE_APP, accountEnabled = false))
    }
}
