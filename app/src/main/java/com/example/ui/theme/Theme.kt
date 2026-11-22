package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PelajariaLightColorScheme = lightColorScheme(
    primary = PelajariaBlueBright,
    onPrimary = Color.White,
    primaryContainer = PelajariaBlueSoft,
    onPrimaryContainer = PelajariaBlueDeep,
    secondary = PelajariaOrange,
    onSecondary = Color.White,
    secondaryContainer = PelajariaAmberSoft,
    onSecondaryContainer = Color(0xFF7A4100),
    tertiary = PelajariaTeal,
    onTertiary = Color.White,
    tertiaryContainer = PelajariaTealSoft,
    onTertiaryContainer = Color(0xFF004D40),
    background = SurfaceLight,
    onBackground = InkPrimary,
    surface = SurfaceCardLight,
    onSurface = InkPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = InkSecondary,
    outline = OutlineSubtle,
    error = PelajariaCoral,
    onError = Color.White,
    errorContainer = PelajariaCoralSoft,
    onErrorContainer = Color(0xFF8C1D18)
)

private val PelajariaDarkColorScheme = darkColorScheme(
    primary = PelajariaBlueDark,
    onPrimary = Color(0xFF062E6F),
    primaryContainer = PelajariaBlue,
    onPrimaryContainer = PelajariaBlueSoft,
    secondary = PelajariaAmber,
    onSecondary = Color(0xFF3E2700),
    secondaryContainer = Color(0xFF5D3A00),
    onSecondaryContainer = PelajariaAmberSoft,
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF003731),
    background = SurfaceDark,
    onBackground = InkLight,
    surface = SurfaceCardDark,
    onSurface = InkLight,
    surfaceVariant = Color(0xFF293548),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

val PelajariaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun PelajariaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PelajariaDarkColorScheme else PelajariaLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PelajariaTypography,
        shapes = PelajariaShapes,
        content = content
    )
}
