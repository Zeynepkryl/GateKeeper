package com.zeynep.gatekeeper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalGKColors = staticCompositionLocalOf { LightGKColors }
internal val LocalGKTypography = staticCompositionLocalOf { gkTypography() }
internal val LocalGKFonts = staticCompositionLocalOf { DefaultGKFonts }
internal val LocalGKSpacing = staticCompositionLocalOf { DefaultGKSpacing }

object GateKeeperTheme {

    val colors: GKColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGKColors.current

    val typography: GKTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalGKTypography.current

    val fonts: GKFonts
        @Composable
        @ReadOnlyComposable
        get() = LocalGKFonts.current

    val spacing: GKSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalGKSpacing.current
}

private fun GKColors.toMaterialLightScheme() = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    background = background,
    surface = surface,
    surfaceVariant = surfaceVariant,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onBackground = textPrimary,
    onSurface = textPrimary,
    onSurfaceVariant = textSecondary,
    outline = divider,
)

private fun GKColors.toMaterialDarkScheme() = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    background = background,
    surface = surface,
    surfaceVariant = surfaceVariant,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onBackground = textPrimary,
    onSurface = textPrimary,
    onSurfaceVariant = textSecondary,
    outline = divider,
)

@Composable
fun GateKeeperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val gkColors = if (darkTheme) DarkGKColors else LightGKColors
    val gkFonts = DefaultGKFonts
    val gkTypography = gkTypography(gkFonts)
    val materialColorScheme = if (darkTheme) {
        gkColors.toMaterialDarkScheme()
    } else {
        gkColors.toMaterialLightScheme()
    }

    CompositionLocalProvider(
        LocalGKColors provides gkColors,
        LocalGKTypography provides gkTypography,
        LocalGKFonts provides gkFonts,
        LocalGKSpacing provides DefaultGKSpacing,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content,
        )
    }
}
