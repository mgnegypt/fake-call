package com.mgneg.fakecall.update

/**
 * Single source of truth for the update source: the project's GitHub
 * releases. (Owner/repo as specified in the implementation plan.)
 */
object UpdateConfig {
    const val OWNER = "mgneg"
    const val REPO = "fake-call"
    const val LATEST_RELEASE_URL = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    const val RELEASES_PAGE_URL = "https://github.com/$OWNER/$REPO/releases"
    const val CHECK_INTERVAL_MS = 12L * 60 * 60 * 1000 // 12 hours
}
