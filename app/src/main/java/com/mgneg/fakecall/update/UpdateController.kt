package com.mgneg.fakecall.update

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

/** Why an update flow failed. Mapped to user-facing strings by the UI. */
enum class UpdateFailReason {
    NETWORK, NOT_ENOUGH_STORAGE, CHECKSUM_UNAVAILABLE, CHECKSUM_MISMATCH, INSTALL_FAILED
}

sealed interface UpdateState {
    data object Idle : UpdateState

    /** [pct] is null when the server did not report a size. */
    data class Downloading(val pct: Int?) : UpdateState
    data object CheckingChecksum : UpdateState
    data object Verifying : UpdateState
    data object Installing : UpdateState
    data object AwaitingConfirm : UpdateState
    data class Failed(val reason: UpdateFailReason) : UpdateState
}

/** Outcomes reported by InstallResultReceiver back into the flow. */
sealed interface InstallOutcome {
    data object AwaitingConfirm : InstallOutcome
    data object Aborted : InstallOutcome
    data object Succeeded : InstallOutcome
    data class Failed(val statusMessage: String?) : InstallOutcome
}

/**
 * Bridge from the manifest-registered [InstallResultReceiver] (created
 * by the system, so it cannot reference the controller) to the flow.
 */
object InstallResults {
    private val _flow = MutableSharedFlow<InstallOutcome>(extraBufferCapacity = 4)
    val flow: SharedFlow<InstallOutcome> = _flow.asSharedFlow()
    fun report(outcome: InstallOutcome) {
        _flow.tryEmit(outcome)
    }
}

/** Where the controller gets its APK + checksum from (faked in tests). */
interface ApkSource {
    suspend fun downloadApk(info: UpdateInfo, onProgress: (Int?) -> Unit): File?
    suspend fun fetchExpectedSha(info: UpdateInfo): String?
    fun cancel()
}

/** Hands a verified APK to the installer (faked in tests). */
fun interface ApkInstaller {
    fun install(apk: File)
}

/**
 * Owns the whole update flow (download → checksum → verify → install)
 * outside any composable, on the app scope: closing the update sheet no
 * longer kills a download. Owned by [UpdateRepository].
 *
 * The checksum rule from plan item 6 is unchanged and fails CLOSED: no
 * expected checksum, or a mismatch, means no install, ever.
 */
class UpdateController(
    private val scope: CoroutineScope,
    private val downloader: ApkSource,
    private val installer: ApkInstaller
) {
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var job: Job? = null
    @Volatile private var currentFile: File? = null

    init {
        // Install outcomes arrive asynchronously from the receiver and
        // drive the terminal states (addendum U7): the flow no longer
        // pretends success the moment the session commits.
        scope.launch {
            InstallResults.flow.collect { outcome ->
                when (outcome) {
                    InstallOutcome.AwaitingConfirm ->
                        _state.value = UpdateState.AwaitingConfirm
                    InstallOutcome.Aborted, InstallOutcome.Succeeded -> {
                        currentFile = null
                        _state.value = UpdateState.Idle
                    }
                    is InstallOutcome.Failed ->
                        _state.value = UpdateState.Failed(UpdateFailReason.INSTALL_FAILED)
                }
            }
        }
    }

    /**
     * Starts the flow from Idle/Failed. The compare-and-set is the
     * double-tap guard: a second start while busy is a no-op.
     */
    fun start(info: UpdateInfo) {
        while (true) {
            val current = _state.value
            if (current !is UpdateState.Idle && current !is UpdateState.Failed) return
            if (_state.compareAndSet(current, UpdateState.Downloading(0))) break
        }
        job = scope.launch { runFlow(info) }
    }

    fun cancel() {
        job?.cancel()
        job = null
        downloader.cancel()
        currentFile?.delete()
        currentFile = null
        _state.value = UpdateState.Idle
    }

    private suspend fun runFlow(info: UpdateInfo) {
        try {
            val file = downloader.downloadApk(info) { pct ->
                _state.value = UpdateState.Downloading(pct)
            }
            if (file == null) {
                _state.value = UpdateState.Failed(UpdateFailReason.NETWORK)
                return
            }
            currentFile = file
            // Visible phases, all heavy work off the main thread (U4): the
            // checksum fetch can take up to 15 s, the verify reads the
            // whole APK, and the install copies + fsyncs it.
            _state.value = UpdateState.CheckingChecksum
            val expected = downloader.fetchExpectedSha(info)
            // Fail CLOSED with a specific reason (plan item 6 + U6): no
            // checksum at all is a different failure from a mismatch.
            if (expected.isNullOrBlank()) {
                file.delete()
                currentFile = null
                _state.value = UpdateState.Failed(UpdateFailReason.CHECKSUM_UNAVAILABLE)
                return
            }
            _state.value = UpdateState.Verifying
            val verified = withContext(Dispatchers.IO) {
                UpdateGate.shouldInstall(expected, file)
            }
            if (!verified) {
                file.delete()
                currentFile = null
                _state.value = UpdateState.Failed(UpdateFailReason.CHECKSUM_MISMATCH)
                return
            }
            _state.value = UpdateState.Installing
            try {
                withContext(Dispatchers.IO) { installer.install(file) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Installer failures (IO, SecurityException, session
                // errors) are reported, never a crash (U6).
                _state.value = UpdateState.Failed(UpdateFailReason.INSTALL_FAILED)
                return
            }
            // Stay in Installing: InstallResultReceiver reports the
            // real outcome (confirm prompt, abort, failure, success)
            // through InstallResults and settles the state (U7).
        } catch (e: CancellationException) {
            throw e // cancellation is not a failure (U6)
        } catch (e: NotEnoughStorageException) {
            _state.value = UpdateState.Failed(UpdateFailReason.NOT_ENOUGH_STORAGE)
        } catch (e: IOException) {
            _state.value = UpdateState.Failed(UpdateFailReason.NETWORK)
        } catch (e: Exception) {
            _state.value = UpdateState.Failed(UpdateFailReason.NETWORK)
        }
    }
}
