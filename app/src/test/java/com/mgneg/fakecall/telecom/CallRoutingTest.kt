package com.mgneg.fakecall.telecom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallRoutingTest {

    @Test
    fun `app mode always uses the app screen regardless of placement`() {
        assertEquals(
            IncomingRoute.APP_SCREEN,
            CallRouting.decide(CallRouting.MODE_APP, placed = false)
        )
        assertEquals(
            IncomingRoute.APP_SCREEN,
            CallRouting.decide(CallRouting.MODE_APP, placed = true)
        )
        // Unknown / legacy values behave like the app screen, never Telecom.
        assertEquals(
            IncomingRoute.APP_SCREEN,
            CallRouting.decide("something-else", placed = true)
        )
    }

    @Test
    fun `telecom mode with successful placement uses telecom`() {
        assertEquals(
            IncomingRoute.TELECOM,
            CallRouting.decide(CallRouting.MODE_TELECOM, placed = true)
        )
    }

    @Test
    fun `telecom mode with failed placement is unavailable and never falls back`() {
        assertEquals(
            IncomingRoute.TELECOM_UNAVAILABLE,
            CallRouting.decide(CallRouting.MODE_TELECOM, placed = false)
        )
    }

    @Test
    fun `placement is attempted unconditionally in telecom mode and never in app mode`() {
        // The registrar — not an enabled-probe on our side — decides
        // whether a telecom call goes through.
        assertTrue(CallRouting.shouldAttemptPlacement(CallRouting.MODE_TELECOM))
        assertFalse(CallRouting.shouldAttemptPlacement(CallRouting.MODE_APP))
        assertFalse(CallRouting.shouldAttemptPlacement("something-else"))
    }
}
