package com.mgneg.fakecall.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionTest {

    @Test
    fun `parses two and three part versions`() {
        assertEquals(Version(1, 0, 0), Version.parse("1.0"))
        assertEquals(Version(1, 2, 0), Version.parse("v1.2"))
        assertEquals(Version(1, 1, 1), Version.parse("1.1.1"))
        assertEquals(Version(2, 10, 3), Version.parse("v2.10.3"))
    }

    @Test
    fun `rejects malformed versions`() {
        assertNull(Version.parse(""))
        assertNull(Version.parse("1"))
        assertNull(Version.parse("1.x"))
        assertNull(Version.parse("a.b"))
        assertNull(Version.parse("1.-2"))
        assertNull(Version.parse("1.2.3.4"))
    }

    @Test
    fun `compares numerically not lexicographically`() {
        assertTrue(Version.parse("1.10")!! > Version.parse("1.9")!!)
        assertTrue(Version.parse("1.1.1")!! > Version.parse("1.1")!!)
        assertTrue(Version.parse("2.0")!! > Version.parse("1.9.9")!!)
        assertEquals(0, Version.parse("1.0")!!.compareTo(Version.parse("1.0.0")!!))
    }
}
