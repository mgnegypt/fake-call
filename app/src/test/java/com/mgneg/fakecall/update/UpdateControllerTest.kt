package com.mgneg.fakecall.update

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

/**
 * Addendum U5 + U11: the update flow's state machine, exercised
 * end-to-end through fake source/installer seams. The checksum gate is
 * the REAL [UpdateGate] + [Sha256], so the fail-closed rules from plan
 * item 6 are covered exactly as shipped.
 */
class UpdateControllerTest {

    private val info = UpdateInfo(Version(9, 9), "", "", "", "", "")

    private class FakeSource(
        var apk: File? = null,
        var sha: String? = null,
        var downloadBody: (suspend ((Int?) -> Unit) -> File?)? = null
    ) : ApkSource {
        val downloads = AtomicInteger(0)
        @Volatile var cancelled = false
        @Volatile var downloadFinallyRan = false

        override suspend fun downloadApk(info: UpdateInfo, onProgress: (Int?) -> Unit): File? {
            downloads.incrementAndGet()
            val body = downloadBody
            return if (body != null) {
                try {
                    body(onProgress)
                } finally {
                    downloadFinallyRan = true
                }
            } else {
                delay(20)
                apk
            }
        }

        override suspend fun fetchExpectedSha(info: UpdateInfo): String? = sha
        override fun cancel() {
            cancelled = true
        }
    }

    private class FakeInstaller(var failure: Exception? = null) : ApkInstaller {
        val installed = mutableListOf<File>()
        override fun install(apk: File) {
            failure?.let { throw it }
            installed += apk
        }
    }

    private fun controller(source: FakeSource, installer: FakeInstaller) = UpdateController(
        scope = CoroutineScope(Job() + Dispatchers.Default),
        downloader = source,
        installer = installer
    )

    private suspend fun awaitState(
        controller: UpdateController,
        predicate: (UpdateState) -> Boolean
    ): UpdateState {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            val s = controller.state.value
            if (predicate(s)) return s
            delay(10)
        }
        return controller.state.value
    }

    private fun tempApk(bytes: ByteArray = byteArrayOf(1, 2, 3)): File {
        val dir = Files.createTempDirectory("fc-u11").toFile()
        return File(dir, "fake-call-v9.9.apk").apply { writeBytes(bytes) }
    }

    @Test
    fun happyPathInstallsAndSettlesOnReceiverSuccess() = runBlocking {
        val apk = tempApk()
        val source = FakeSource(apk = apk, sha = Sha256.hex(apk))
        val installer = FakeInstaller()
        val controller = controller(source, installer)

        controller.start(info)
        awaitState(controller) { it is UpdateState.Installing }
        assertEquals(listOf(apk), installer.installed)
        // The flow waits in Installing until the receiver reports (U7).
        InstallResults.report(InstallOutcome.Succeeded)
        val settled = awaitState(controller) { it is UpdateState.Idle }
        assertTrue(settled is UpdateState.Idle)
    }

    @Test
    fun missingChecksumFailsClosedAndNeverInstalls() = runBlocking {
        val source = FakeSource(apk = tempApk(), sha = null)
        val installer = FakeInstaller()
        val controller = controller(source, installer)

        controller.start(info)
        val end = awaitState(controller) { it is UpdateState.Failed }
        assertEquals(
            UpdateState.Failed(UpdateFailReason.CHECKSUM_UNAVAILABLE), end
        )
        assertTrue(installer.installed.isEmpty())
    }

    @Test
    fun checksumMismatchDeletesTheFileAndNeverInstalls() = runBlocking {
        val apk = tempApk()
        val source = FakeSource(apk = apk, sha = "0".repeat(64))
        val installer = FakeInstaller()
        val controller = controller(source, installer)

        controller.start(info)
        val end = awaitState(controller) { it is UpdateState.Failed }
        assertEquals(UpdateState.Failed(UpdateFailReason.CHECKSUM_MISMATCH), end)
        assertTrue(installer.installed.isEmpty())
        assertFalse("mismatched APK must be deleted", apk.exists())
    }

    @Test
    fun doubleStartRunsOneDownload() = runBlocking {
        val source = FakeSource(apk = tempApk(), sha = null)
        val controller = controller(source, FakeInstaller())

        controller.start(info)
        controller.start(info) // the double-tap: must be a no-op
        awaitState(controller) { it is UpdateState.Failed }
        assertEquals(1, source.downloads.get())
    }

    @Test
    fun cancelStopsTheDownloadAndReturnsToIdle() = runBlocking {
        val source = FakeSource(downloadBody = { _ ->
            delay(60_000) // a slow network; cancel must interrupt it
            null
        })
        val controller = controller(source, FakeInstaller())

        controller.start(info)
        awaitState(controller) { source.downloads.get() == 1 }
        delay(50)
        controller.cancel()
        assertTrue(source.cancelled)
        assertTrue(controller.state.value is UpdateState.Idle)
        // The in-flight download coroutine is cancelled, not left running.
        val deadline = System.currentTimeMillis() + 5_000
        while (!source.downloadFinallyRan && System.currentTimeMillis() < deadline) {
            delay(10)
        }
        assertTrue(source.downloadFinallyRan)
    }

    @Test
    fun installExceptionBecomesFailedInstallNotACrash() = runBlocking {
        val apk = tempApk()
        val source = FakeSource(apk = apk, sha = Sha256.hex(apk))
        val installer = FakeInstaller(failure = IOException("session died"))
        val controller = controller(source, installer)

        controller.start(info)
        val end = awaitState(controller) { it is UpdateState.Failed }
        assertEquals(UpdateState.Failed(UpdateFailReason.INSTALL_FAILED), end)
    }

    @Test
    fun receiverFailureSettlesInstallingAsInstallFailed() = runBlocking {
        val apk = tempApk()
        val source = FakeSource(apk = apk, sha = Sha256.hex(apk))
        val controller = controller(source, FakeInstaller())

        controller.start(info)
        awaitState(controller) { it is UpdateState.Installing }
        InstallResults.report(InstallOutcome.Failed("INSTALL_FAILED_UPDATE_INCOMPATIBLE"))
        val end = awaitState(controller) { it is UpdateState.Failed }
        assertEquals(UpdateState.Failed(UpdateFailReason.INSTALL_FAILED), end)
    }

    @Test
    fun progressReportsNullForUnknownSize() = runBlocking {
        // Each progress value is held for a window so the test can poll
        // the state value directly — collecting a StateFlow here would
        // race its conflation and flake.
        val source = FakeSource(downloadBody = { onProgress ->
            onProgress(null) // unknown size (U3)
            delay(400)
            onProgress(50)
            delay(400)
            null // then the download "fails" so the flow ends
        })
        val controller = controller(source, FakeInstaller())

        controller.start(info)
        val unknown = awaitState(controller) {
            it is UpdateState.Downloading && it.pct == null
        }
        assertTrue(unknown is UpdateState.Downloading && unknown.pct == null)
        val half = awaitState(controller) {
            it is UpdateState.Downloading && it.pct == 50
        }
        assertTrue(half is UpdateState.Downloading && half.pct == 50)
        awaitState(controller) { it is UpdateState.Failed }
    }
}
