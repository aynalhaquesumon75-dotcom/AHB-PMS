package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLanguage

private val AhbDarkColorScheme = darkColorScheme(
    primary = BrightBlue,
    onPrimary = TextPrimary,
    primaryContainer = ElectricBlue,
    onPrimaryContainer = TextPrimary,
    secondary = RoyalPurple,
    onSecondary = TextPrimary,
    secondaryContainer = ColorTokens.PurpleContainer,
    onSecondaryContainer = SoftViolet,
    tertiary = CyanAccent,
    onTertiary = NavyVoid,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = NavyVoid,
    surfaceContainerLow = NavySurface,
    surfaceContainer = NavySurfaceElevated,
    surfaceContainerHigh = NavySurfaceHighlight,
    outline = NavyBorder,
    outlineVariant = NavyBorderSubtle,
    error = CoralError,
    onError = TextPrimary,
    errorContainer = CoralContainer,
    onErrorContainer = ColorTokens.ErrorText
)

private object ColorTokens {
    val PurpleContainer = androidx.compose.ui.graphics.Color(0xFF2E1065)
    val ErrorText = androidx.compose.ui.graphics.Color(0xFFFCA5A5)
}

val AhbShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun AhbPmsTheme(
    language: AppLanguage = AppLanguage.EN,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AhbDarkColorScheme,
        typography = buildAppTypography(isBengali = language == AppLanguage.BN),
        shapes = AhbShapes,
        content = content
    )
}
