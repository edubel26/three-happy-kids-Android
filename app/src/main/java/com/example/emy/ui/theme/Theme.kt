package com.example.emy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = BrandDark,
    secondary = Color(0xFFE8B496),
    tertiary = Color(0xFF8ACA9B),
    background = PageDark,
    surface = SurfaceDark,
    onPrimary = Color(0xFF2C1A12),
    onBackground = InkDark,
    onSurface = InkDark,
    onSurfaceVariant = Color(0xFFBBB6AD),
    primaryContainer = Color(0xFF382820),
    onPrimaryContainer = InkDark,
    secondaryContainer = Color(0xFF3A302A),
    onSecondaryContainer = InkDark
)

private val LightColorScheme = lightColorScheme(
    primary = Brand,
    secondary = BrandStrong,
    tertiary = Success,
    background = Page,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Muted,
    outline = Border,
    primaryContainer = BrandSoft,
    onPrimaryContainer = Ink,
    secondaryContainer = Color(0xFFF1EFEB),
    onSecondaryContainer = Ink
)

private val HappyKidsShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp)
)

@Composable
fun EmyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = HappyKidsShapes,
        content = content
    )
}