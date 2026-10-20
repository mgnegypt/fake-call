package com.mgneg.fakecall.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val downloadUrl: String
)

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val name: String = "",
    val body: String = "",
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    @SerialName("published_at") val publishedAt: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    val assets: List<GitHubAsset> = emptyList()
)

/** The pair of assets the updater needs for one version. */
data class ReleaseAssets(val apk: GitHubAsset, val sha256: GitHubAsset?)

object ReleaseParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseRelease(jsonText: String): GitHubRelease? =
        runCatching { json.decodeFromString<GitHubRelease>(jsonText) }.getOrNull()

    /** A release counts only if it is published, stable, and newer than [current]. */
    fun isNewer(current: Version, release: GitHubRelease): Boolean {
        if (release.draft || release.prerelease) return false
        val v = Version.parse(release.tagName) ?: return false
        return v > current
    }

    /** Expected APK name for a version, per the release naming rules. */
    fun apkName(version: Version): String = "fake-call-v$version.apk"

    fun selectAssets(release: GitHubRelease): ReleaseAssets? {
        val version = Version.parse(release.tagName) ?: return null
        val apk = release.assets.firstOrNull { it.name == apkName(version) } ?: return null
        val sha = release.assets.firstOrNull { it.name == apkName(version) + ".sha256" }
        return ReleaseAssets(apk, sha)
    }

    /**
     * Extract the release-notes section for [language] ("ar" or "en") from a
     * bilingual release body. Sections are introduced by "## English" and
     * "## العربية". Falls back to English, then to the raw body.
     */
    fun notesFor(body: String, language: String): String {
        if (body.isBlank()) return ""
        val en = section(body, "## English", listOf("## العربية"))
        val ar = section(body, "## العربية", listOf("## English"))
        val chosen = if (language.startsWith("ar")) (ar ?: en) else (en ?: ar)
        return (chosen ?: body).trim()
    }

    private fun section(body: String, header: String, otherHeaders: List<String>): String? {
        val start = body.indexOf(header)
        if (start < 0) return null
        val contentStart = start + header.length
        var end = body.length
        for (other in otherHeaders) {
            val idx = body.indexOf(other, contentStart)
            if (idx in contentStart until end) end = idx
        }
        return body.substring(contentStart, end)
    }
}
