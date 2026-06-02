package com.aetherchat.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    background = AppColors.Light.Background,
    surface = AppColors.Light.Surface,
    onBackground = AppColors.Light.OnBackground,
    onSurface = AppColors.Light.OnSurface,
    surfaceVariant = AppColors.Light.Surface,
    outline = AppColors.Light.Divider,
    outlineVariant = AppColors.Light.Divider,
)

private val DarkColorScheme = darkColorScheme(
    background = AppColors.Dark.Background,
    surface = AppColors.Dark.Surface,
    onBackground = AppColors.Dark.OnBackground,
    onSurface = AppColors.Dark.OnSurface,
    surfaceVariant = AppColors.Dark.Surface,
    outline = AppColors.Dark.Divider,
    outlineVariant = AppColors.Dark.Divider,
)

@Composable
fun AetherChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        typography = AetherChatTypography,
        content = content,
    )
}
