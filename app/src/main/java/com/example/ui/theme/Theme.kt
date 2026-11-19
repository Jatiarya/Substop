package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SubStopLightColorScheme = lightColorScheme(
    primary = SubStopPrimary,
    onPrimary = Color.White,
    primaryContainer = SubStopPrimaryTint,
    onPrimaryContainer = SubStopPrimaryDeep,
    secondary = SubStopSecondary,
    onSecondary = Color.White,
    secondaryContainer = SubStopSecondaryTint,
    onSecondaryContainer = SubStopSecondaryText,
    tertiary = SubStopTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SubStopTertiaryTint,
    onTertiaryContainer = SubStopTertiaryText,
    error = SubStopError,
    onError = Color.White,
    errorContainer = SubStopErrorTint,
    onErrorContainer = SubStopErrorText,
    background = SubStopBackground,
    onBackground = Slate900,
    surface = SubStopSurface,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate300,
    outlineVariant = Slate200
)

val SubStopShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun SubStopTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SubStopLightColorScheme,
        typography = SubStopTypography,
        shapes = SubStopShapes,
        content = content
    )
}
