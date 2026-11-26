package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val TacticalDarkColorScheme = darkColorScheme(
    primary = ApexOrange,
    onPrimary = ThunderInk,
    primaryContainer = ApexOrangeGlow,
    onPrimaryContainer = ApexOrange,
    secondary = CyanTelemetry,
    onSecondary = ObsidianBg,
    secondaryContainer = CyanTelemetryGlow,
    onSecondaryContainer = CyanTelemetry,
    tertiary = OptimalGreen,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ElevatedSurface,
    onSurface = TextPrimary,
    surfaceVariant = TacticalCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = ElevatedSurface,
    surfaceContainer = TacticalCard,
    surfaceContainerHigh = TacticalCardBright,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle,
    error = CriticalRed,
    onError = TextPrimary,
)

val TacticalShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun FFApexBoostTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TacticalDarkColorScheme,
        typography = AppTypography,
        shapes = TacticalShapes,
        content = content,
    )
}
