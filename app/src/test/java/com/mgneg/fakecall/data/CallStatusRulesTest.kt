package com.mgneg.fakecall.data

import com.mgneg.fakecall.data.db.CallStatus
import com.mgneg.fakecall.data.repo.CallStatusRules
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallStatusRulesTest {

    @Test
    fun `happy path chain is allowed`() {
        assertTrue(CallStatusRules.allowed(CallStatus.SCHEDULED, CallStatus.RINGING))
        assertTrue(CallStatusRules.allowed(CallStatus.RINGING, CallStatus.ANSWERED))
        assertTrue(CallStatusRules.allowed(CallStatus.ANSWERED, CallStatus.COMPLETED))
    }

    @Test
    fun `ringing out is a miss, never after an answer`() {
        assertTrue(CallStatusRules.allowed(CallStatus.RINGING, CallStatus.MISSED))
        assertTrue(CallStatusRules.allowed(CallStatus.SCHEDULED, CallStatus.MISSED))
        assertFalse(CallStatusRules.allowed(CallStatus.ANSWERED, CallStatus.MISSED))
    }

    @Test
    fun `decline and failure finish a live row`() {
        assertTrue(CallStatusRules.allowed(CallStatus.RINGING, CallStatus.DECLINED))
        assertTrue(CallStatusRules.allowed(CallStatus.ANSWERED, CallStatus.DECLINED))
        assertTrue(CallStatusRules.allowed(CallStatus.SCHEDULED, CallStatus.FAILED))
        assertTrue(CallStatusRules.allowed(CallStatus.RINGING, CallStatus.FAILED))
    }

    @Test
    fun `settled rows never move again`() {
        for (terminal in listOf(
            CallStatus.COMPLETED, CallStatus.DECLINED,
            CallStatus.MISSED, CallStatus.FAILED
        )) {
            for (target in listOf(
                CallStatus.RINGING, CallStatus.ANSWERED,
                CallStatus.COMPLETED, CallStatus.MISSED
            )) {
                assertFalse(
                    "$terminal -> $target must be rejected",
                    CallStatusRules.allowed(terminal, target)
                )
            }
        }
    }

    @Test
    fun `skipping stages is rejected`() {
        assertFalse(CallStatusRules.allowed(CallStatus.SCHEDULED, CallStatus.ANSWERED))
        assertFalse(CallStatusRules.allowed(CallStatus.ANSWERED, CallStatus.RINGING))
        assertFalse(CallStatusRules.allowed(CallStatus.MISSED, CallStatus.RINGING))
    }
}
