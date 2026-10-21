package com.mgneg.fakecall.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mgneg.fakecall.R

/**
 * Cairo (OFL) in the three weights the design uses: 500 / 700 / 900.
 * Bundled as font resources; if a resource fails to load, Compose falls
 * back to the platform default family for that style.
 */
val CairoFamily = FontFamily(
    Font(R.font.cairo_medium, FontWeight.Medium),
    Font(R.font.cairo_bold, FontWeight.Bold),
    Font(R.font.cairo_black, FontWeight.Black)
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int = (size * 1.35).toInt()) =
    TextStyle(
        fontFamily = CairoFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp
    )

/** Sizes/weights follow the design type scale (plan section 2). */
val FakeCallTypography = Typography(
    headlineMedium = style(28, FontWeight.Black), // screen title 28/900
    headlineSmall = style(24, FontWeight.Black),
    titleLarge = style(20, FontWeight.Bold),
    titleMedium = style(15, FontWeight.Bold), // row title 15/700
    titleSmall = style(14, FontWeight.Bold),
    bodyLarge = style(16, FontWeight.Bold), // field value 16/700
    bodyMedium = style(14, FontWeight.Medium),
    bodySmall = style(12, FontWeight.Medium), // helper text 12 (muted)
    labelLarge = style(14, FontWeight.Bold),
    labelMedium = style(12, FontWeight.Bold),
    labelSmall = style(11, FontWeight.Medium) // field label 11 (muted)
)
