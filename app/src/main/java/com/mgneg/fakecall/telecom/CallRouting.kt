package com.mgneg.fakecall.telecom

/** Where a triggered incoming call ends up being presented. */
enum class IncomingRoute {
    /** The system phone app renders the call through the Telecom account. */
    TELECOM,

    /** Our own call screen renders the call (only when the user chose it). */
    APP_SCREEN,

    /**
     * The user chose the Telecom presentation but it cannot be used right
     * now (account disabled or placement rejected). Nothing rings; the
     * user is told honestly instead of silently getting a different UI.
     */
    TELECOM_UNAVAILABLE
}

/**
 * Pure routing decision for [CallController.startIncoming], kept free of
 * Android types so it is unit-testable on the JVM.
 *
 * The two settings modes are strict:
 * - `"app"` always uses the app call screen; the Telecom account state
 *   and placement result are irrelevant and ignored.
 * - `"telecom"` uses Telecom ONLY. A disabled account or a failed
 *   placement yields [IncomingRoute.TELECOM_UNAVAILABLE] — never a silent
 *   substitution of the app screen, which would mask the real problem and
 *   present a UI the user did not ask for.
 *
 * [placed] is only consulted when the mode is `"telecom"` and the account
 * is enabled (the only combination under which placement is attempted).
 */
object CallRouting {

    const val MODE_TELECOM = "telecom"
    const val MODE_APP = "app"

    fun decide(mode: String, accountEnabled: Boolean, placed: Boolean): IncomingRoute =
        when {
            mode != MODE_TELECOM -> IncomingRoute.APP_SCREEN
            !accountEnabled -> IncomingRoute.TELECOM_UNAVAILABLE
            placed -> IncomingRoute.TELECOM
            else -> IncomingRoute.TELECOM_UNAVAILABLE
        }

    /** Placement is only worth attempting for a live Telecom route. */
    fun shouldAttemptPlacement(mode: String, accountEnabled: Boolean): Boolean =
        mode == MODE_TELECOM && accountEnabled
}
