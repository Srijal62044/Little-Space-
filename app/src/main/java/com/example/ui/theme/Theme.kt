package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun getDarkColorScheme(themeName: String): ColorScheme {
    return when (themeName) {
        "Matcha Sage" -> darkColorScheme(
            primary = Color(0xFF6EE7B7),
            onPrimary = Color(0xFF064E3B),
            primaryContainer = Color(0xFF133824),
            onPrimaryContainer = Color(0xFFA7F3D0),
            secondary = Color(0xFFA7F3D0),
            onSecondary = Color(0xFF0F291E),
            secondaryContainer = Color(0xFF1B4332),
            onSecondaryContainer = Color(0xFFD1FAE5),
            tertiary = Color(0xFFFDE68A),
            background = Color(0xFF0E1712),
            onBackground = Color(0xFFE6F4EA),
            surface = Color(0xFF16231B),
            onSurface = Color(0xFFE6F4EA),
            surfaceVariant = Color(0xFF22352B),
            onSurfaceVariant = Color(0xFFA3BFB0),
            outline = Color(0xFF385244)
        )
        "Lavender Dream" -> darkColorScheme(
            primary = Color(0xFFC4B5FD),
            onPrimary = Color(0xFF4C1D95),
            primaryContainer = Color(0xFF321A58),
            onPrimaryContainer = Color(0xFFEDE9FE),
            secondary = Color(0xFFDDD6FE),
            onSecondary = Color(0xFF2E1065),
            secondaryContainer = Color(0xFF3B2366),
            onSecondaryContainer = Color(0xFFF5F3FF),
            tertiary = BlushPrimarySoft,
            background = Color(0xFF120E1C),
            onBackground = Color(0xFFF3E8FF),
            surface = Color(0xFF1B152A),
            onSurface = Color(0xFFF3E8FF),
            surfaceVariant = Color(0xFF28203D),
            onSurfaceVariant = Color(0xFFBFB3DC),
            outline = Color(0xFF4C3E6C)
        )
        "Warm Peach" -> darkColorScheme(
            primary = Color(0xFFFDBA74),
            onPrimary = Color(0xFF7C2D12),
            primaryContainer = Color(0xFF4A1B0B),
            onPrimaryContainer = Color(0xFFFFEDD5),
            secondary = Color(0xFFFED7AA),
            onSecondary = Color(0xFF431407),
            secondaryContainer = Color(0xFF592312),
            onSecondaryContainer = Color(0xFFFFF7ED),
            tertiary = Color(0xFFA7F3D0),
            background = Color(0xFF18100C),
            onBackground = Color(0xFFFEE8DB),
            surface = Color(0xFF241813),
            onSurface = Color(0xFFFEE8DB),
            surfaceVariant = Color(0xFF36241D),
            onSurfaceVariant = Color(0xFFD5AEA0),
            outline = Color(0xFF5C3C30)
        )
        "Cozy Latte" -> darkColorScheme(
            primary = Color(0xFFFDE68A),
            onPrimary = Color(0xFF713F12),
            primaryContainer = Color(0xFF452B10),
            onPrimaryContainer = Color(0xFFFEF3C7),
            secondary = Color(0xFFFCD34D),
            onSecondary = Color(0xFF451A03),
            secondaryContainer = Color(0xFF523314),
            onSecondaryContainer = Color(0xFFFFFBEB),
            tertiary = BlushPrimarySoft,
            background = Color(0xFF16120E),
            onBackground = Color(0xFFF9EFE4),
            surface = Color(0xFF221C16),
            onSurface = Color(0xFFF9EFE4),
            surfaceVariant = Color(0xFF332A22),
            onSurfaceVariant = Color(0xFFCBB7A5),
            outline = Color(0xFF56473A)
        )
        else -> darkColorScheme( // Default "Rose Blush"
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
    }
}

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
    isDarkMode: Boolean = true,
    followSystem: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = if (followSystem) systemDark else isDarkMode

    val colorScheme = if (useDarkTheme) {
        getDarkColorScheme(selectedTheme)
    } else {
        getLightColorScheme(selectedTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
