package com.mgneg.fakecall.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.mgneg.fakecall.FakeCallApp
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * Housekeeping for the private answer-audio copies in filesDir/audio.
 * Picked audio is copied there under a timestamped name; without cleanup
 * every replacement would leak the previous copy forever.
 */
object AudioFiles {

    fun dir(context: Context): File = File(context.filesDir, "audio").apply { mkdirs() }

    /** The human-readable name of a picked document (its display name,
     * not a content-provider path segment). */
    fun displayName(context: Context, uri: Uri): String? =
        runCatching {
            context.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else {
                    null
                }
            }
        }.getOrNull()

    /** True while any preset or still-scheduled call points at [path]. */
    suspend fun isReferenced(context: Context, path: String): Boolean {
        val app = context.applicationContext as FakeCallApp
        val presets = app.database.presetDao().observeAll().first()
        if (presets.any { sameFile(it.audioUri, path) }) return true
        val scheduled = app.database.callHistoryDao().pendingScheduled()
        return scheduled.any { sameFile(it.audioUri, path) }
    }

    private fun sameFile(storedUri: String?, path: String): Boolean {
        if (storedUri.isNullOrBlank()) return false
        if (storedUri == path) return true
        return runCatching { Uri.parse(storedUri).path == path }.getOrDefault(false)
    }

    /** Delete one of our private copies unless something still uses it. */
    suspend fun deleteIfUnreferenced(context: Context, path: String?) {
        if (path.isNullOrBlank()) return
        val file = File(path)
        if (!file.exists()) return
        // Only ever delete inside our own audio directory.
        if (file.parentFile?.absolutePath != dir(context).absolutePath) return
        if (!isReferenced(context, path)) file.delete()
    }

    /** App-start sweep: drop every private copy nothing references. */
    suspend fun cleanupUnreferenced(context: Context) {
        val files = dir(context).listFiles() ?: return
        for (file in files) {
            if (file.isFile && !isReferenced(context, file.absolutePath)) {
                file.delete()
            }
        }
    }
}
