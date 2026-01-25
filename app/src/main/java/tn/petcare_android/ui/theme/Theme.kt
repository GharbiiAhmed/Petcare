package tn.petcare_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import tn.petcare_android.R
import tn.petcare_android.ui.components.BackgroundBox

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBrown, // Primary Brown #6B4F3F
    secondary = SecondaryIvory, // Secondary Ivory #F7F4EF
    tertiary = AccentSoftGreen, // Accent Soft Green #7FB685
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = White,
    onSecondary = Color(0xFF2F2F2F), // Text Primary #2F2F2F on secondary
    onTertiary = White,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBrown, // Primary Brown #6B4F3F
    secondary = SecondaryIvory, // Secondary Ivory #F7F4EF
    tertiary = AccentSoftGreen, // Accent Soft Green #7FB685
    background = SecondaryIvory,  // Secondary Ivory background
    surface = Color(0xFFFFFFFF),     // CardBackground - White
    surfaceVariant = SecondaryIvory, // HeaderBackground - Secondary Ivory
    onPrimary = Color.White,         // White text on primary
    onSecondary = Color(0xFF2F2F2F), // Text Primary #2F2F2F on secondary
    onTertiary = Color.White,  // White text on tertiary
    onBackground = Color(0xFF2F2F2F), // Text Primary #2F2F2F
    onSurface = Color(0xFF2F2F2F),   // Text Primary #2F2F2F
    onSurfaceVariant = Color(0xFF6E6E6E), // Text Secondary #6E6E6E
    error = ErrorRed
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Select background image and color based on theme
    val backgroundColor = if (darkTheme) DarkBackground else SecondaryIvory
    val backgroundImageRes = if (darkTheme) R.drawable.darkmode else R.drawable.paw_background

    // Wrap with background image (under layer)
    // The background image appears as an under layer on all screens automatically
    // Light mode: paw_background.png, Dark mode: darkmode.png
    BackgroundBox(
        backgroundColor = backgroundColor,
        backgroundImageRes = backgroundImageRes
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
