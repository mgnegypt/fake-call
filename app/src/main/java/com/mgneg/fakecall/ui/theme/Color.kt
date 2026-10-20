package com.mgneg.fakecall.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Dual visual identity tokens.
 *
 * Volt (dark): bold, high-contrast, technical — deep black surfaces, vivid
 * lime primary. Aura (light): calm, organic, natural — warm off-white
 * surfaces, sage-green primary.
 *
 * Anchor colors come from the design brief; the remaining Material 3 roles
 * are tonal derivations of those anchors so every component state
 * (containers, inverse, error, disabled) stays inside the same identity.
 */

// ---- Volt anchors ----
val VoltPrimary = Color(0xFFD8FF24)
val VoltBackground = Color(0xFF111111)
val VoltSurface = Color(0xFF1C1C1C)
val VoltElevated = Color(0xFF292929)
val VoltOnSurface = Color(0xFFFFFFFF)
val VoltOnSurfaceVariant = Color(0xFFC7C7C7)
val VoltOutline = Color(0xFF454545)

// ---- Aura anchors ----
val AuraPrimary = Color(0xFF365C3D)
val AuraBackground = Color(0xFFF4F4E8)
val AuraSurface = Color(0xFFFFF9E9)
val AuraElevated = Color(0xFFE6EBD8)
val AuraOnSurface = Color(0xFF243A2A)
val AuraOnSurfaceVariant = Color(0xFF52624D)
val AuraAccent = Color(0xFFA5D88B)
val AuraOutline = Color(0xFFD5DDC8)

/** Fixed brand tile behind the app icon (About header, drawer header).
 *  A deep botanical green that reads correctly in both identities. */
val BrandTile = Color(0xFF365C3D)

/** Splash background anchors (mirrored in res/values[-night]/colors.xml). */
val SplashLight = AuraBackground
val SplashDark = VoltBackground
