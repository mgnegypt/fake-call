package com.mgneg.fakecall.update

import com.mgneg.fakecall.BuildConfig
import com.mgneg.fakecall.data.settings.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val version: Version,
    val notesBody: String,
    val releaseUrl: String,
    val apkUrl: String,
    val shaUrl: String,
    val date: String
)

/** Checks GitHub Releases (ETag-cached) and mirrors the result into DataStore. */
class UpdateChecker(private val settings: SettingsStore) {

    suspend fun check(manual: Boolean): UpdateInfo? = withContext(Dispatchers.IO) {
        val cache = settings.updateCache.first()
        val now = System.currentTimeMillis()
        if (!manual && now - cache.lastCheck < UpdateConfig.CHECK_INTERVAL_MS) {
            return@withContext cache.toUpdateInfo()
        }
        val current = Version.parse(BuildConfig.VERSION_NAME) ?: return@withContext null
        runCatching {
            val conn = (URL(UpdateConfig.LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 15_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "FakeCall/${BuildConfig.VERSION_NAME}")
                cache.etag?.let { setRequestProperty("If-None-Match", it) }
            }
            try {
                when (conn.responseCode) {
                    HttpURLConnection.HTTP_NOT_MODIFIED -> {
                        settings.saveUpdateCache(cache.copy(lastCheck = now))
                        cache.toUpdateInfo()
                    }
                    HttpURLConnection.HTTP_OK -> {
                        val text = conn.inputStream.bufferedReader().readText()
                        val release = ReleaseParser.parseRelease(text)
                        if (release == null || !ReleaseParser.isNewer(current, release)) {
                            settings.saveUpdateCache(
                                SettingsStore.UpdateCache(lastCheck = now, etag = conn.getHeaderField("ETag"))
                            )
                            null
                        } else {
                            val version = Version.parse(release.tagName)!!
                            val assets = ReleaseParser.selectAssets(release)
                            val info = UpdateInfo(
                                version = version,
                                notesBody = release.body,
                                releaseUrl = release.htmlUrl.ifBlank { UpdateConfig.RELEASES_PAGE_URL },
                                apkUrl = assets?.apk?.downloadUrl ?: "",
                                shaUrl = assets?.sha256?.downloadUrl ?: "",
                                date = release.publishedAt.take(10)
                            )
                            settings.saveUpdateCache(
                                SettingsStore.UpdateCache(
                                    lastCheck = now,
                                    etag = conn.getHeaderField("ETag"),
                                    latestVersion = version.toString(),
                                    notes = release.body,
                                    releaseUrl = info.releaseUrl,
                                    apkUrl = info.apkUrl,
                                    shaUrl = info.shaUrl,
                                    date = info.date
                                )
                            )
                            info
                        }
                    }
                    else -> cache.toUpdateInfo() // rate limit / server error: stay silent
                }
            } finally {
                conn.disconnect()
            }
        }.getOrNull() ?: cache.toUpdateInfo()
    }

    private fun SettingsStore.UpdateCache.toUpdateInfo(): UpdateInfo? {
        val v = latestVersion?.let { Version.parse(it) } ?: return null
        val current = Version.parse(BuildConfig.VERSION_NAME) ?: return null
        if (v <= current) return null
        return UpdateInfo(v, notes, releaseUrl, apkUrl, shaUrl, date)
    }
}
