package com.mgneg.fakecall.update

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/** Why an update flow failed. Mapped to user-facing strings by the UI. */
enum class UpdateFailReason { NETWORK, CHECKSUM, INSTALL }

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
    private val downloader: UpdateDownloader,
    private val install: (File) -> Unit
) {
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var job: Job? = null
    @Volatile private var currentFile: File? = null

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
        val file = downloader.downloadApk(info) { pct ->
            _state.value = UpdateState.Downloading(pct)
        }
        if (file == null) {
            _state.value = UpdateState.Failed(UpdateFailReason.NETWORK)
            return
        }
        currentFile = file
        _state.value = UpdateState.Verifying
        val expected = downloader.fetchExpectedSha(info)
        if (!UpdateGate.shouldInstall(expected, file)) {
            file.delete()
            currentFile = null
            _state.value = UpdateState.Failed(UpdateFailReason.CHECKSUM)
            return
        }
        _state.value = UpdateState.Installing
        install(file)
        currentFile = null
        _state.value = UpdateState.Idle
    }
}
