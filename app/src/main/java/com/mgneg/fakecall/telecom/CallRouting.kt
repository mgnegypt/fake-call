package com.mgneg.fakecall.telecom

/** Where a triggered incoming call ends up being presented. */
enum class IncomingRoute {
    /** The system phone app renders the call through the Telecom account. */
    TELECOM,

    /** Our own call screen renders the call (only when the user chose it). */
    APP_SCREEN,

    /**
     * The user chose the Telecom presentation but placement failed (the
     * framework rejected or dropped the call). Nothing rings; the user is
     * told honestly instead of silently getting a different UI.
     */
    TELECOM_UNAVAILABLE
}

/**
 * Pure routing decision for [CallController.startIncoming], kept free of
 * Android types so it is unit-testable on the JVM.
 *
 * The two settings modes are strict:
 * - `"app"` always uses the app call screen; placement is never attempted.
 * - `"telecom"` uses Telecom ONLY, and placement is attempted
 *   UNCONDITIONALLY — the registrar decides whether the call goes through,
 *   not a pre-check on our side. An earlier version gated placement on an
 *   `accountEnabled` probe; on at least one device that probe read false
 *   while the system settings showed the account toggle ON, and every
 *   trigger died at the gate without Telecom ever being asked. The
 *   enabled state is still observed and logged for diagnostics, but it
 *   no longer gates anything.
 *
 * A failed placement yields [IncomingRoute.TELECOM_UNAVAILABLE] — never a
 * silent substitution of the app screen, which would mask the real
 * problem and present a UI the user did not ask for.
 */
object CallRouting {

    const val MODE_TELECOM = "telecom"
    const val MODE_APP = "app"

    fun decide(mode: String, placed: Boolean): IncomingRoute =
        when {
            mode != MODE_TELECOM -> IncomingRoute.APP_SCREEN
            placed -> IncomingRoute.TELECOM
            else -> IncomingRoute.TELECOM_UNAVAILABLE
        }

    /**
     * Telecom mode always attempts placement (the framework is the
     * authority on whether the account can take the call); app mode
     * never touches Telecom.
     */
    fun shouldAttemptPlacement(mode: String): Boolean = mode == MODE_TELECOM
}
