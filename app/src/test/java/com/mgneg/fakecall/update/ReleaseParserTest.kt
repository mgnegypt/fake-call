package com.mgneg.fakecall.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseParserTest {

    private val sampleJson = """
        {
          "tag_name": "v1.2",
          "name": "Fake Call v1.2",
          "body": "notes",
          "draft": false,
          "prerelease": false,
          "published_at": "2026-11-01T10:00:00Z",
          "html_url": "https://github.com/mgneg/fake-call/releases/tag/v1.2",
          "assets": [
            {"name": "fake-call-v1.2.apk", "browser_download_url": "https://example.com/fake-call-v1.2.apk"},
            {"name": "fake-call-v1.2.apk.sha256", "browser_download_url": "https://example.com/fake-call-v1.2.apk.sha256"},
            {"name": "other.zip", "browser_download_url": "https://example.com/other.zip"}
          ],
          "unknown_field": 42
        }
    """.trimIndent()

    @Test
    fun `parses github release json`() {
        val release = ReleaseParser.parseRelease(sampleJson)
        assertNotNull(release)
        assertEquals("v1.2", release!!.tagName)
        assertEquals(3, release.assets.size)
    }

    @Test
    fun `malformed json returns null`() {
        assertNull(ReleaseParser.parseRelease("{not json"))
        assertNull(ReleaseParser.parseRelease(""))
    }

    @Test
    fun `selects apk and checksum assets by exact name`() {
        val release = ReleaseParser.parseRelease(sampleJson)!!
        val assets = ReleaseParser.selectAssets(release)
        assertNotNull(assets)
        assertEquals("fake-call-v1.2.apk", assets!!.apk.name)
        assertEquals("fake-call-v1.2.apk.sha256", assets.sha256?.name)
    }

    @Test
    fun `missing apk asset returns null`() {
        val release = ReleaseParser.parseRelease(sampleJson)!!
            .copy(assets = listOf(GitHubAsset("other.zip", "https://example.com/other.zip")))
        assertNull(ReleaseParser.selectAssets(release))
    }

    @Test
    fun `drafts and prereleases are not updates`() {
        val base = ReleaseParser.parseRelease(sampleJson)!!
        val current = Version(1, 0)
        assertTrue(ReleaseParser.isNewer(current, base))
        assertFalse(ReleaseParser.isNewer(current, base.copy(draft = true)))
        assertFalse(ReleaseParser.isNewer(current, base.copy(prerelease = true)))
        assertFalse(ReleaseParser.isNewer(Version(1, 2), base))
        assertFalse(ReleaseParser.isNewer(current, base.copy(tagName = "nightly")))
    }

    private val bilingualBody = """
        ## English
        ### Added
        - Quick presets
        ### Fixed
        - Crash on Android 11

        ## العربية
        ### تمت الإضافة
        - إعدادات سريعة
    """.trimIndent()

    @Test
    fun `notes parser picks the requested language`() {
        val en = ReleaseParser.notesFor(bilingualBody, "en")
        assertTrue(en.contains("Quick presets"))
        assertFalse(en.contains("إعدادات"))
        val ar = ReleaseParser.notesFor(bilingualBody, "ar")
        assertTrue(ar.contains("إعدادات سريعة"))
        assertFalse(ar.contains("Quick presets"))
    }

    @Test
    fun `notes parser falls back gracefully`() {
        // Arabic requested but only English exists -> English text.
        val onlyEn = "## English\n### Added\n- X"
        assertTrue(ReleaseParser.notesFor(onlyEn, "ar").contains("- X"))
        // No headers at all -> raw body.
        assertEquals("plain body", ReleaseParser.notesFor("plain body", "ar"))
        // Empty body -> empty.
        assertEquals("", ReleaseParser.notesFor("", "en"))
    }
}
