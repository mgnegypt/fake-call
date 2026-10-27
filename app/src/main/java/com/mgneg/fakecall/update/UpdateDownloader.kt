package com.mgneg.fakecall.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Downloads the release APK (and its checksum) into the app cache. */
class UpdateDownloader(context: Context) {

    private val dir = File(context.cacheDir, "updates").apply { mkdirs() }

    @Volatile
    private var cancelled = false

    fun cancel() {
        cancelled = true
    }

    suspend fun downloadApk(info: UpdateInfo, onProgress: (Int?) -> Unit): File? {
        cancelled = false
        val target = File(dir, ReleaseParser.apkName(info.version))
        val ok = download(info.apkUrl, target, onProgress)
        return if (ok) target else null
    }

    suspend fun fetchExpectedSha(info: UpdateInfo): String? = withContext(Dispatchers.IO) {
        if (info.shaUrl.isBlank()) return@withContext null
        runCatching {
            val conn = URL(info.shaUrl).openConnection() as HttpURLConnection
            try {
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                if (conn.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
                val text = conn.inputStream.bufferedReader().readText()
                Sha256.expectedFromFileContent(text)
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }

    private suspend fun download(url: String, target: File, onProgress: (Int?) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val conn = URL(url).openConnection() as HttpURLConnection
                try {
                    conn.connectTimeout = 20_000
                    conn.readTimeout = 30_000
                    if (conn.responseCode != HttpURLConnection.HTTP_OK) return@runCatching false
                    val total = conn.contentLengthLong
                    // Unknown size: report null once so the UI shows
                    // an indeterminate bar instead of sitting at 0%.
                    if (total <= 0) onProgress(null)
                    var lastPct = -1
                    conn.inputStream.use { input ->
                        target.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var done = 0L
                            while (true) {
                                if (cancelled) return@runCatching false
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
            }.getOrDefault(false)
        }
}
