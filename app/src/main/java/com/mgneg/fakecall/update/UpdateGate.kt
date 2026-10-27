package com.mgneg.fakecall.update

import java.io.File

/**
 * The install decision for a downloaded update. Verification fails
 * CLOSED: without a known expected checksum there is no install, ever —
 * a missing or unreachable .sha256 asset must not silently downgrade a
 * "verified" update to an unverified one.
 */
object UpdateGate {

    fun shouldInstall(expectedSha256: String?, downloadedApk: File): Boolean {
        if (expectedSha256.isNullOrBlank()) return false
        return Sha256.verify(downloadedApk, expectedSha256)
    }
}
