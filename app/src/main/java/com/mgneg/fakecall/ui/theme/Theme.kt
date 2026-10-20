package com.mgneg.fakecall.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Hand-made static Material 3 scheme (deep teal), used on Android 11 and
// whenever dynamic color is off or unavailable.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0B5A7A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC2E8FF),
    onPrimaryContainer = Color(0xFF001E2C),
    secondary = Color(0xFF4E616D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1E5F2),
    onSecondaryContainer = Color(0xFF0A1E28),
    tertiary = Color(0xFF5E5786),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE3DFFF),
    onTertiaryContainer = Color(0xFF1B1836),
    background = Color(0xFFFBFCFE),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFBFCFE),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDCE4EA),
    onSurfaceVariant = Color(0xFF40484E),
    outline = Color(0xFF70787E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8ED0F5),
    onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF0B4A66),
    onPrimaryContainer = Color(0xFFC2E8FF),
    secondary = Color(0xFFB5C9D6),
    onSecondary = Color(0xFF21333E),
    secondaryContainer = Color(0xFF374955),
    onSecondaryContainer = Color(0xFFD1E5F2),
    tertiary = Color(0xFFC6C0EA),
    onTertiary = Color(0xFF2F2A4D),
    tertiaryContainer = Color(0xFF454066),
    onTertiaryContainer = Color(0xFFE3DFFF),
    background = Color(0xFF111417),
    onBackground = Color(0xFFE1E3E5),
    surface = Color(0xFF111417),
    onSurface = Color(0xFFE1E3E5),
    surfaceVariant = Color(0xFF40484E),
    onSurfaceVariant = Color(0xFFC0C8CE),
    outline = Color(0xFF899297)
)

@Composable
fun FakeCallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
