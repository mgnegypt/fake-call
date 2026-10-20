package com.mgneg.fakecall.util

/**
 * Developer contact links, provided by the project owner.
 * A button is hidden in the About screen while its value is a placeholder.
 */
object ContactLinks {
    const val FACEBOOK_URL = "https://www.facebook.com/mgnegypt"
    const val WHATSAPP_URL = "https://wa.me/mgneg"
    const val INSTAGRAM_URL = "https://www.instagram.com/mgnegypt"
    const val X_URL = "https://x.com/MGN_EGYPT"

    const val GITHUB_REPO_URL = "https://github.com/mgneg/fake-call"
    const val PLACEHOLDER = "<TO_BE_PROVIDED>"

    fun isSet(url: String): Boolean = url.isNotBlank() && !url.contains(PLACEHOLDER)
}
