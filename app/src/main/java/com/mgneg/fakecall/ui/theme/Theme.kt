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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Volt UI — the dark identity. Lime primary on deep black/charcoal,
 * crisp shapes, maximum contrast.
 */
val VoltColorScheme = darkColorScheme(
    primary = VoltPrimary,
    onPrimary = Color(0xFF111111),
    primaryContainer = Color(0xFF2E3D00),
    onPrimaryContainer = VoltPrimary,
    secondary = Color(0xFFB9C48F),
    onSecondary = Color(0xFF1A2105),
    secondaryContainer = VoltElevated,
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFFE9FF8A),
    onTertiary = Color(0xFF232A00),
    tertiaryContainer = Color(0xFF3D4708),
    onTertiaryContainer = Color(0xFFE9FF8A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = VoltBackground,
    onBackground = VoltOnSurface,
    surface = VoltSurface,
    onSurface = VoltOnSurface,
    surfaceVariant = VoltElevated,
    onSurfaceVariant = VoltOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = Color(0xFF161616),
    surfaceContainer = VoltSurface,
    surfaceContainerHigh = VoltElevated,
    surfaceContainerHighest = Color(0xFF333333),
    inverseSurface = AuraBackground,
    inverseOnSurface = Color(0xFF111111),
    inversePrimary = AuraPrimary,
    outline = VoltOutline,
    outlineVariant = Color(0xFF333333),
    scrim = Color(0xFF000000)
)

/**
 * Aura UI — the light identity. Sage primary on warm off-white,
 * soft elevated surfaces, rounded shapes.
 */
val AuraColorScheme = lightColorScheme(
    primary = AuraPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E8C8),
    onPrimaryContainer = Color(0xFF1E3623),
    secondary = AuraOnSurfaceVariant,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = AuraElevated,
    onSecondaryContainer = AuraOnSurface,
    tertiary = Color(0xFF4E7A43),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDFF3CE),
    onTertiaryContainer = Color(0xFF223D1B),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = AuraBackground,
    onBackground = AuraOnSurface,
    surface = AuraSurface,
    onSurface = AuraOnSurface,
    surfaceVariant = AuraElevated,
    onSurfaceVariant = AuraOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF6E9),
    surfaceContainer = AuraSurface,
    surfaceContainerHigh = Color(0xFFF1EFDB),
    surfaceContainerHighest = AuraElevated,
    inverseSurface = AuraOnSurface,
    inverseOnSurface = AuraBackground,
    inversePrimary = AuraAccent,
    outline = AuraOutline,
    outlineVariant = Color(0xFFC4CFB4),
    scrim = Color(0xFF000000)
)

/** Volt: crisp, deliberate corners. */
val VoltShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

/** Aura: soft, organic corners. */
val AuraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * One product, two identities:
 * - Dark (system or explicit) -> Volt UI
 * - Light -> Aura UI
 * Dynamic color stays available as a user opt-in from Settings; when it is
 * off (the default), the brand palettes above define the experience.
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
        darkTheme -> VoltColorScheme
        else -> AuraColorScheme
    }
    MaterialTheme(
        colorScheme = colors,
        shapes = if (darkTheme) VoltShapes else AuraShapes,
        content = content
    )
}
