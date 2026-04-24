package nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = primary,
    onSecondary = onPrimary,
    secondaryContainer = bgCard,
    onSecondaryContainer = textPrimary,
    tertiary = stateFinished,
    onTertiary = onPrimary,
    tertiaryContainer = bgActive,
    onTertiaryContainer = textPrimary,
    background = bgMain,
    onBackground = textPrimary,
    surface = bgCard,
    onSurface = textPrimary,
    surfaceVariant = bgActive,
    onSurfaceVariant = textSecondary,
    outline = divider,
    outlineVariant = divider,
    error = stateAlert,
    onError = onPrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = primaryDark,
    onSecondary = onPrimaryDark,
    secondaryContainer = bgActiveDark,
    onSecondaryContainer = textPrimaryDark,
    tertiary = stateFinishedDark,
    onTertiary = onPrimaryDark,
    tertiaryContainer = bgActiveDark,
    onTertiaryContainer = textPrimaryDark,
    background = bgMainDark,
    onBackground = textPrimaryDark,
    surface = bgCardDark,
    onSurface = textPrimaryDark,
    surfaceVariant = bgActiveDark,
    onSurfaceVariant = textSecondaryDark,
    outline = dividerDark,
    outlineVariant = dividerDark,
    error = stateAlertDark,
    onError = onPrimaryDark,
)

@Composable
fun PageKeeperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

