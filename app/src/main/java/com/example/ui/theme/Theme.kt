package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GlowingNeonMint,
    secondary = PrestigeBronzeGold,
    tertiary = CloverMint,
    background = DeepSpruce,
    surface = MossCardBg,
    onPrimary = DeepSpruce,
    onSecondary = DeepSpruce,
    onTertiary = DeepSpruce,
    onBackground = DarkOffWhite,
    onSurface = DarkOffWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PremiumForestGreen,
    secondary = TerracottaClay,
    tertiary = CloverMint,
    background = WarmSandCream,
    surface = PureWinterWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF13221C),
    onSurface = Color(0xFF13221C)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful Agri-Tech branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
