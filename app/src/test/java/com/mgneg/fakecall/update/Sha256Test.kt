package com.mgneg.fakecall.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Sha256Test {

    @Test
    fun `hashes known content`() {
        // SHA-256 of "abc"
        val hex = Sha256.hex("abc".byteInputStream())
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            hex
        )
    }

    @Test
    fun `parses sha256 file content`() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Sha256.expectedFromFileContent(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad  fake-call-v1.0.apk"
            )
        )
        assertNull(Sha256.expectedFromFileContent("not a hash"))
        assertNull(Sha256.expectedFromFileContent(""))
    }

    @Test
    fun `verifies a file against an expected hash`() {
        val file = File.createTempFile("sha-test", ".bin")
        try {
            file.writeText("abc")
            assertTrue(
                Sha256.verify(
                    file,
                    "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
                )
            )
            assertFalse(Sha256.verify(file, "0".repeat(64)))
        } finally {
            file.delete()
        }
    }
}
