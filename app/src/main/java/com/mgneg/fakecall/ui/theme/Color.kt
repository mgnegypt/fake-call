package com.mgneg.fakecall.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Burgundy "soft glass" design tokens — the single visual identity for
 * both light and dark mode (replaces the retired Volt/Aura pair).
 *
 * Values are fixed by the design package (html/theme.css is the source of
 * truth; the plan's section-2 table matches it). Material 3 roles live in
 * Theme.kt; everything the roles cannot express (gradient stops, glow,
 * translucent card, tint, danger surfaces…) is carried by [BurgundyColors]
 * and provided through a CompositionLocal in Theme.kt.
 */
data class BurgundyColors(
    val bgTop: Color,
    val bgBottom: Color,
    val glow: Color,
    /** Card surface; translucent white in light mode, solid in dark. */
    val card: Color,
    /** Menus / popups surface. */
    val card2: Color,
    /** Input field fill. */
    val field: Color,
    val text: Color,
    val muted: Color,
    /** Icon bubbles, selected rows/nav pill. */
    val tint: Color,
    /** Accent fill (gradient end, switches, selected chips). */
    val accent: Color,
    /** Accent dark (gradient start). */
    val accentDark: Color,
    /** Accent for text/icons sitting on cards (dark-mode safe). */
    val accentText: Color,
    val divider: Color,
    val shadowSoft: Color,
    val shadowStrong: Color,
    /** Floating bottom bar surface. */
    val nav: Color,
    /** Ring around the centre FAB. */
    val fabRing: Color,
    val danger: Color,
    val dangerBg: Color,
    val isDark: Boolean
)

val BurgundyLight = BurgundyColors(
    bgTop = Color(0xFFF8F3F5),
    bgBottom = Color(0xFFE9DEE4),
    glow = Color(0xFFF7D6E2),
    card = Color(0xE0FFFFFF), // #FFFFFF @ 88%
    card2 = Color(0xFFFFFFFF),
    field = Color(0xFFF5EAEE),
    text = Color(0xFF2A1119),
    muted = Color(0xFF6F5A62),
    tint = Color(0xFFF8DCE6),
    accent = Color(0xFF8A1B3F),
    accentDark = Color(0xFF4F0C24),
    accentText = Color(0xFF8A1B3F),
    divider = Color(0xFFE6D6DD),
    shadowSoft = Color(0x1A5A142D), // rgba(90,20,45,.10)
    shadowStrong = Color(0x405A142D), // rgba(90,20,45,.25)
    nav = Color(0xEBFFFFFF), // #FFFFFF @ 92%
    fabRing = Color(0xFFF3EAEE),
    danger = Color(0xFFB4182F),
    dangerBg = Color(0xFFFBE0E4),
    isDark = false
)

val BurgundyDark = BurgundyColors(
    bgTop = Color(0xFF1B0F14),
    bgBottom = Color(0xFF120A0E),
    glow = Color(0xFF4A1226),
    card = Color(0xFF251519),
    card2 = Color(0xFF2E1C22),
    field = Color(0xFF2F1D23),
    text = Color(0xFFF7ECF0),
    muted = Color(0xFFBCA6AE),
    tint = Color(0xFF44202E),
    accent = Color(0xFF8A1B3F),
    accentDark = Color(0xFF4F0C24),
    accentText = Color(0xFFF28AAA),
    divider = Color(0xFF43303A),
    shadowSoft = Color(0x66000000), // rgba(0,0,0,.40)
    shadowStrong = Color(0x99000000), // rgba(0,0,0,.60)
    nav = Color(0xFF251519),
    fabRing = Color(0xFF1B0F14),
    danger = Color(0xFFFF8FA0),
    dangerBg = Color(0xFF4A1B25),
    isDark = true
)

/** Notification badge red — fixed in both modes. */
val BadgeRed = Color(0xFFD6243F)

/** Splash background anchors (mirrored in res/values[-night]/colors.xml). */
val SplashLight = BurgundyLight.bgTop
val SplashDark = BurgundyDark.bgTop
