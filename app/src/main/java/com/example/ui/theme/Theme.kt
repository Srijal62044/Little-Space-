package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BlushPrimarySoft,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1828),
    onPrimaryContainer = SoftRose,
    secondary = SoftRose,
    onSecondary = DarkBackground,
    secondaryContainer = Color(0xFF3B242C),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = SoftSage,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private fun getLightColorScheme(themeName: String): ColorScheme {
    return when (themeName) {
        "Matcha Sage" -> lightColorScheme(
            primary = SagePrimary,
            onPrimary = Color.White,
            primaryContainer = SageSurfaceVariantLight,
            onPrimaryContainer = SageSecondary,
            secondary = SagePrimarySoft,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD1FAE5),
            onSecondaryContainer = Color(0xFF065F46),
            tertiary = BlushPrimarySoft,
            background = SageBackgroundLight,
            onBackground = Color(0xFF132A1C),
            surface = Color.White,
            onSurface = Color(0xFF132A1C),
            surfaceVariant = SageSurfaceVariantLight,
            onSurfaceVariant = Color(0xFF4B6354),
            outline = Color(0xFFD1E7DD)
        )
        "Lavender Dream" -> lightColorScheme(
            primary = LavenderPrimary,
            onPrimary = Color.White,
            primaryContainer = LavenderSurfaceVariantLight,
            onPrimaryContainer = LavenderSecondary,
            secondary = LavenderPrimarySoft,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFEDE9FE),
            onSecondaryContainer = Color(0xFF5B21B6),
            tertiary = BlushPrimarySoft,
            background = LavenderBackgroundLight,
            onBackground = Color(0xFF1E162B),
            surface = Color.White,
            onSurface = Color(0xFF1E162B),
            surfaceVariant = LavenderSurfaceVariantLight,
            onSurfaceVariant = Color(0xFF5E546F),
            outline = Color(0xFFE2DCF5)
        )
        "Warm Peach" -> lightColorScheme(
            primary = PeachPrimary,
            onPrimary = Color.White,
            primaryContainer = PeachSurfaceVariantLight,
            onPrimaryContainer = PeachSecondary,
            secondary = PeachPrimarySoft,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFEDD5),
            onSecondaryContainer = Color(0xFF9A3412),
            tertiary = SoftSageDark,
            background = PeachBackgroundLight,
            onBackground = Color(0xFF2C1911),
            surface = Color.White,
            onSurface = Color(0xFF2C1911),
            surfaceVariant = PeachSurfaceVariantLight,
            onSurfaceVariant = Color(0xFF6B5347),
            outline = Color(0xFFFED7AA)
        )
        "Cozy Latte" -> lightColorScheme(
            primary = LattePrimary,
            onPrimary = Color.White,
            primaryContainer = LatteSurfaceVariantLight,
            onPrimaryContainer = LatteSecondary,
            secondary = LattePrimarySoft,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFEF3C7),
            onSecondaryContainer = Color(0xFF78350F),
            tertiary = BlushPrimarySoft,
            background = LatteBackgroundLight,
            onBackground = Color(0xFF2B2017),
            surface = Color.White,
            onSurface = Color(0xFF2B2017),
            surfaceVariant = LatteSurfaceVariantLight,
            onSurfaceVariant = Color(0xFF6B584B),
            outline = Color(0xFFE5D5C5)
        )
        else -> lightColorScheme( // Default "Geometric Balance" / "Rose Blush"
            primary = GeoRoseAccent,
            onPrimary = Color.White,
            primaryContainer = GeoRoseContainer,
            onPrimaryContainer = GeoRoseOnContainer,
            secondary = GeoLavenderText,
            onSecondary = Color.White,
            secondaryContainer = GeoLavenderContainer,
            onSecondaryContainer = GeoLavenderText,
            tertiary = GeoMintText,
            onTertiary = Color.White,
            tertiaryContainer = GeoMintContainer,
            onTertiaryContainer = GeoMintText,
            background = GeoCanvas,
            onBackground = GeoTextPrimary,
            surface = Color.White,
            onSurface = GeoTextPrimary,
            surfaceVariant = GeoItemBg,
            onSurfaceVariant = GeoTextSecondary,
            outline = GeoBorder
        )
    }
}

@Composable
fun LittleSpaceTheme(
    selectedTheme: String = "Rose Blush",
    isDarkMode: Boolean = false,
    followSystem: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = if (followSystem) systemDark else isDarkMode

    val colorScheme = if (useDarkTheme) {
        DarkColorScheme
    } else {
        getLightColorScheme(selectedTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
