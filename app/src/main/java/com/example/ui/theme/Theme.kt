package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    background = PremiumBackground,
    onBackground = PremiumOnBackground,
    surface = PremiumSurface,
    onSurface = PremiumOnBackground,
    surfaceVariant = PremiumSurfaceVariant,
    onSurfaceVariant = PremiumOnSurfaceVariant,
    primary = PremiumPrimary,
    onPrimary = Color.White,
    primaryContainer = PremiumPrimary,
    onPrimaryContainer = Color.White,
    secondary = PremiumSecondary,
    onSecondary = Color.White,
    secondaryContainer = PremiumSecondary,
    onSecondaryContainer = Color.White,
    outline = PremiumOutline,
    outlineVariant = PremiumOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    background = LightPremiumBackground,
    onBackground = LightPremiumOnBackground,
    surface = LightPremiumSurface,
    onSurface = LightPremiumOnBackground,
    surfaceVariant = LightPremiumSurfaceVariant,
    onSurfaceVariant = LightPremiumOnSurfaceVariant,
    primary = PremiumPrimary,
    onPrimary = Color.White,
    primaryContainer = PremiumPrimary,
    onPrimaryContainer = Color.White,
    secondary = PremiumSecondary,
    onSecondary = Color.White,
    secondaryContainer = PremiumSecondary,
    onSecondaryContainer = Color.White,
    outline = LightPremiumOutline,
    outlineVariant = LightPremiumOutlineVariant
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
