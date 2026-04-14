package com.tinlera.toolbox.ui.theme

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

private val Teal = Color(0xFF00BFA5)
private val TealDark = Color(0xFF00897B)

private val DarkColorScheme = darkColorScheme(
    primary = Teal,
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFFF6E40),
    surface = Color(0xFF1C1B1F),
    background = Color(0xFF121212),
)

private val LightColorScheme = lightColorScheme(
    primary = TealDark,
    secondary = Color(0xFF00695C),
    tertiary = Color(0xFFDD2C00),
    surface = Color(0xFFFFFBFE),
    background = Color(0xFFF5F5F5),
)

@Composable
fun MethunToolboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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
        content = content
    )
}
