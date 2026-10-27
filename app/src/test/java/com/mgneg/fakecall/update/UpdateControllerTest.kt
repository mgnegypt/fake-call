package com.mgneg.fakecall.update

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

/** Addendum U5: the double-start guard, exercised through fakes. */
class UpdateControllerTest {

    private class FakeSource(private val apk: File?) : ApkSource {
        val downloads = AtomicInteger(0)
        override suspend fun downloadApk(info: UpdateInfo, onProgress: (Int?) -> Unit): File? {
            downloads.incrementAndGet()
            delay(20)
            return apk
        }
        override suspend fun fetchExpectedSha(info: UpdateInfo): String? = null
        override fun cancel() {}
    }

    @Test
    fun doubleStartRunsOneDownload() = runBlocking {
        val dir = Files.createTempDirectory("fc-u5").toFile()
        val apk = File(dir, "fake-call-v9.9.apk").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val source = FakeSource(apk)
        var installs = 0
        val controller = UpdateController(
            scope = CoroutineScope(Job() + Dispatchers.Default),
            downloader = source,
            installer = ApkInstaller { installs++ }
        )
        val info = UpdateInfo(Version(9, 9), "", "", "", "", "")
        controller.start(info)
        controller.start(info) // the double-tap: must be a no-op
        // The fake reports no checksum, so the flow must fail CLOSED at
        // the checksum gate and never reach the installer.
        val deadline = System.currentTimeMillis() + 5_000
        while (controller.state.value !is UpdateState.Failed &&
            System.currentTimeMillis() < deadline
        ) {
            delay(10)
        }
        assertTrue(controller.state.value is UpdateState.Failed)
        assertEquals(1, source.downloads.get())
        assertEquals(0, installs)
    }
}
