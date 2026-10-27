package com.mgneg.fakecall.data

import com.mgneg.fakecall.data.repo.ReschedulePolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class ReschedulePolicyTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `future trigger is rescheduled`() {
        assertEquals(
            ReschedulePolicy.Action.RESCHEDULE,
            ReschedulePolicy.decide(now + 5_000, now)
        )
        assertEquals(
            ReschedulePolicy.Action.RESCHEDULE,
            ReschedulePolicy.decide(now + 3_600_000, now)
        )
    }

    @Test
    fun `just-missed trigger still fires within the grace window`() {
        assertEquals(
            ReschedulePolicy.Action.FIRE,
            ReschedulePolicy.decide(now, now)
        )
        assertEquals(
            ReschedulePolicy.Action.FIRE,
            ReschedulePolicy.decide(now - ReschedulePolicy.FIRE_GRACE_MS, now)
        )
    }

    @Test
    fun `stale trigger is marked missed and never rings`() {
        assertEquals(
            ReschedulePolicy.Action.MARK_MISSED,
            ReschedulePolicy.decide(now - ReschedulePolicy.FIRE_GRACE_MS - 1, now)
        )
        assertEquals(
            ReschedulePolicy.Action.MARK_MISSED,
            ReschedulePolicy.decide(now - 86_400_000, now)
        )
    }
}
