package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val PriyStudyDarkColorScheme =
  darkColorScheme(
    primary = ElectricBluePrimary,
    onPrimary = OnElectricBluePrimary,
    primaryContainer = ElectricBlueContainer,
    onPrimaryContainer = OnElectricBlueContainer,
    secondary = CyanSecondary,
    onSecondary = OnCyanSecondary,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = OnCyanContainer,
    tertiary = EmeraldTertiary,
    onTertiary = OnEmeraldTertiary,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = OnEmeraldContainer,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = NavyBackground,
    surfaceContainerLow = NavySurface,
    surfaceContainer = NavySurfaceVariant,
    surfaceContainerHigh = NavyElevated,
    outline = NavyOutline,
    outlineVariant = NavyOutlineVariant,
    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
  )

val PriyStudyShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
  )

@Composable
fun PriyStudyTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = PriyStudyDarkColorScheme,
    typography = Typography,
    shapes = PriyStudyShapes,
    content = content,
  )
}
