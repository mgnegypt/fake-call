package com.mgneg.fakecall.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Extended burgundy tokens that Material 3 roles cannot express. */
val LocalBurgundy = staticCompositionLocalOf { BurgundyLight }

val BurgundyLightScheme = lightColorScheme(
    primary = BurgundyLight.accent,
    onPrimary = Color.White,
    primaryContainer = BurgundyLight.tint,
    onPrimaryContainer = BurgundyLight.accentDark,
    secondary = Color(0xFF77555F),
    onSecondary = Color.White,
    secondaryContainer = BurgundyLight.field,
    onSecondaryContainer = BurgundyLight.text,
    tertiary = Color(0xFFC96F8E),
    onTertiary = Color.White,
    tertiaryContainer = BurgundyLight.glow,
    onTertiaryContainer = BurgundyLight.accentDark,
    error = BurgundyLight.danger,
    onError = Color.White,
    errorContainer = BurgundyLight.dangerBg,
    onErrorContainer = BurgundyLight.danger,
    background = BurgundyLight.bgTop,
    onBackground = BurgundyLight.text,
    surface = Color(0xFFFFFFFF),
    onSurface = BurgundyLight.text,
    surfaceVariant = BurgundyLight.field,
    onSurfaceVariant = BurgundyLight.muted,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = BurgundyLight.field,
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = BurgundyLight.field,
    surfaceContainerHighest = BurgundyLight.tint,
    inverseSurface = BurgundyLight.accentDark,
    inverseOnSurface = Color(0xFFF7ECF0),
    inversePrimary = BurgundyLight.tint,
    outline = BurgundyLight.divider,
    outlineVariant = Color(0xFFF0E4EA),
    scrim = Color(0xFF000000)
)

val BurgundyDarkScheme = darkColorScheme(
    primary = BurgundyDark.accent,
    onPrimary = Color.White,
    primaryContainer = BurgundyDark.tint,
    onPrimaryContainer = Color(0xFFF8DCE6),
    secondary = Color(0xFFD8B7C0),
    onSecondary = Color(0xFF2A1119),
    secondaryContainer = BurgundyDark.field,
    onSecondaryContainer = BurgundyDark.text,
    tertiary = Color(0xFFB76E84),
    onTertiary = Color(0xFF2A1119),
    tertiaryContainer = BurgundyDark.glow,
    onTertiaryContainer = Color(0xFFF8DCE6),
    error = BurgundyDark.danger,
    onError = Color(0xFF4A1B25),
    errorContainer = BurgundyDark.dangerBg,
    onErrorContainer = BurgundyDark.danger,
    background = BurgundyDark.bgTop,
    onBackground = BurgundyDark.text,
    surface = BurgundyDark.card,
    onSurface = BurgundyDark.text,
    surfaceVariant = BurgundyDark.field,
    onSurfaceVariant = BurgundyDark.muted,
    surfaceContainerLowest = BurgundyDark.bgBottom,
    surfaceContainerLow = BurgundyDark.card,
    surfaceContainer = BurgundyDark.card,
    surfaceContainerHigh = BurgundyDark.card2,
    surfaceContainerHighest = BurgundyDark.field,
    inverseSurface = Color(0xFFF7ECF0),
    inverseOnSurface = BurgundyDark.accentDark,
    inversePrimary = BurgundyDark.accent,
    outline = BurgundyDark.divider,
    outlineVariant = Color(0xFF33222A),
    scrim = Color(0xFF000000)
)

/** One soft shape scale for both modes (card 24, hero 28 in components). */
val BurgundyShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Burgundy soft-glass theme. [darkTheme] is resolved by the caller from
 * the stored theme-mode preference (system / light / dark); dynamic
 * colour (Monet) stays an opt-in that overrides the burgundy scheme
 * while following the same mode.
 */
@Composable
fun FakeCallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> BurgundyDarkScheme
        else -> BurgundyLightScheme
    }
    // Extended tokens always describe the burgundy identity for the mode;
    // with Monet on, components still use them for brand surfaces.
    val burgundy = if (darkTheme) BurgundyDark else BurgundyLight
    CompositionLocalProvider(LocalBurgundy provides burgundy) {
        MaterialTheme(
            colorScheme = colors,
            shapes = BurgundyShapes,
            typography = FakeCallTypography,
            content = content
        )
    }
}
