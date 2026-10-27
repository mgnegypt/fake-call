package com.mgneg.fakecall.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class UpdateGateTest {

    private fun tempApk(content: String): File {
        val file = Files.createTempFile("update-gate", ".apk").toFile()
        file.writeText(content)
        file.deleteOnExit()
        return file
    }

    @Test
    fun `missing expected checksum never installs`() {
        val file = tempApk("fake apk bytes")
        assertFalse(UpdateGate.shouldInstall(null, file))
        assertFalse(UpdateGate.shouldInstall("", file))
        assertFalse(UpdateGate.shouldInstall("   ", file))
    }

    @Test
    fun `matching checksum installs`() {
        val file = tempApk("fake apk bytes")
        assertTrue(UpdateGate.shouldInstall(Sha256.hex(file), file))
    }

    @Test
    fun `mismatched checksum never installs`() {
        val file = tempApk("fake apk bytes")
        val other = tempApk("different bytes entirely")
        assertFalse(UpdateGate.shouldInstall(Sha256.hex(other), file))
    }
}
