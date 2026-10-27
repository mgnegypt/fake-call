package com.mgneg.fakecall.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Thrown when the cache volume cannot hold the APK (plus a margin). */
class NotEnoughStorageException : IOException("not enough storage for the update")

/** Downloads the release APK (and its checksum) into the app cache. */
class UpdateDownloader(context: Context) : ApkSource {

    private val dir = File(context.cacheDir, "updates").apply { mkdirs() }

    @Volatile
    private var cancelled = false

    override fun cancel() {
        cancelled = true
    }

    override suspend fun downloadApk(info: UpdateInfo, onProgress: (Int?) -> Unit): File? {
        cancelled = false
        val target = File(dir, ReleaseParser.apkName(info.version))
        // Download to a .part file and rename only when complete, so a
        // cancelled or failed download can never leave a corrupt file
        // under the final APK name (addendum U5).
        val part = File(dir, target.name + ".part")
        val ok = download(info.apkUrl, part, onProgress)
        if (!ok) {
            part.delete()
            return null
        }
        if (target.exists()) target.delete()
        return if (part.renameTo(target)) target else {
            part.delete()
            null
        }
    }

    override suspend fun fetchExpectedSha(info: UpdateInfo): String? = withContext(Dispatchers.IO) {
        if (info.shaUrl.isBlank()) return@withContext null
        try {
            val conn = URL(info.shaUrl).openConnection() as HttpURLConnection
            try {
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                val text = conn.inputStream.bufferedReader().readText()
                Sha256.expectedFromFileContent(text)
            } finally {
                conn.disconnect()
            }
        } catch (e: IOException) {
            null // unreachable checksum = fail closed upstream (item 6)
        }
    }

    private suspend fun download(url: String, target: File, onProgress: (Int?) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                try {
                    conn.connectTimeout = 20_000
                    conn.readTimeout = 30_000
                    if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext false
                    val total = conn.contentLengthLong
                    // Fail early, before writing a byte, when the cache
                    // volume cannot hold the APK plus a margin (U6).
                    if (total > 0 && dir.usableSpace < total + total / 10) {
                        throw NotEnoughStorageException()
                    }
                    // Unknown size: report null once so the UI shows
                    // an indeterminate bar instead of sitting at 0%.
                    if (total <= 0) onProgress(null)
                    var lastPct = -1
                    conn.inputStream.use { input ->
                        target.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var done = 0L
                            while (true) {
                                if (cancelled) return@withContext false
                                currentCoroutineContext().ensureActive()
                                val read = input.read(buffer)
                                if (read < 0) break
                                output.write(buffer, 0, read)
                                done += read
                                if (total > 0) {
                                    val pct = (done * 100 / total).toInt()
                                    // Emit only on change: no redundant
                                    // state writes for every 64K chunk.
                                    if (pct != lastPct) {
                                        lastPct = pct
                                        onProgress(pct)
                                    }
                                }
                            }
                        }
                    }
                    true
                } finally {
                    conn.disconnect()
                }
            } catch (e: NotEnoughStorageException) {
                throw e // the controller maps this to its own message
            } catch (e: IOException) {
                false
            }
            // CancellationException is NOT caught here: it propagates
            // and cancels the flow instead of reading as a failure (U6).
        }
}
