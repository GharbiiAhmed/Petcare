package tn.petcare_android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Theme-aware color properties
val PageBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = SecondaryIvory.copy(alpha = 0f) // Fully transparent - allows paw print background to show through

val HeaderBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val CardBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val ScreenBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val LoginBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val InputBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant


private val LightPageBackground = Color(0xFFF7F4EF) // Secondary Ivory
private val LightHeaderBackground = Color(0xFFF7F4EF) // Secondary Ivory
private val LightCardBackground = Color(0xFFFFFFFF)
private val LightScreenBackground = Color(0xFFF7F4EF) // Secondary Ivory
private val LightLoginBackground = Color(0xFFF7F4EF) // Secondary Ivory
private val LightInputBackground = Color(0xFFF7F4EF) // Secondary Ivory

// Primary Colors - New Theme Colors
val PrimaryBrown = Color(0xFF6B4F3F) // Primary Brown
val SecondaryIvory = Color(0xFFF7F4EF) // Secondary Ivory
val AccentSoftGreen = Color(0xFF7FB685) // Accent Soft Green
val AlertMutedOrange = Color(0xFFE09F3E) // Alert Muted Orange

// Legacy color names for backward compatibility (mapped to new colors)
val OrangeAccent = AccentSoftGreen // Accent Soft Green
val OrangeDark = PrimaryBrown // Primary Brown (darker)
val OrangeLight = SecondaryIvory // Secondary Ivory
val OrangePrimary = PrimaryBrown // Primary Brown
val OrangeSplash = PrimaryBrown // Primary Brown for splash screen
val OrangeButton = PrimaryBrown // Primary Brown for buttons
val OrangePawPrint = PrimaryBrown // Primary Brown for paw print background pattern

// Pet Avatar Colors - Updated to complement new theme
val PetAvatarBrown = Color(0xFFD4C4B8) // Light brown
val PetAvatarTan = Color(0xFFB8A08F) // Medium brown
val AvatarBackground = SecondaryIvory // Secondary Ivory

// Text Colors
private val LightTextPrimary = Color(0xFF2F2F2F) // Text Primary
private val LightTextSecondary = Color(0xFF6E6E6E) // Text Secondary
val TextLink = AccentSoftGreen // Accent Soft Green for links

// Chip Colors
val ChipSelectedBg = PrimaryBrown // Primary Brown
val ChipUnselectedBg = Color.White
val ChipSelectedText = Color.White
val ChipUnselectedText = PrimaryBrown // Primary Brown

// Timeline Colors
val TimelineDot = PrimaryBrown // Primary Brown
val TimelineLine = Color(0xFFD4C4B8) // Light brown

// Border Colors
val GreenBorder = AccentSoftGreen // Accent Soft Green
val OrangeBorder = PrimaryBrown // Primary Brown border
val RedBorder = Color(0xFFE74C3C)
val GreyBorder = Color(0xFFE0E0E0)
val GoogleButtonOutline = Color(0xFFE0E0E0)

// Status Colors
val ErrorRed = Color(0xFFE74C3C)
val RedLocation = Color(0xFFE74C3C)
val BlueAccent = AccentSoftGreen // Accent Soft Green
val StarColor = AlertMutedOrange // Alert Muted Orange
val GreenHealthy = AccentSoftGreen // Accent Soft Green

// Bubble Colors (Chat)
val UserBubbleColor = PrimaryBrown // Primary Brown
val AIBubbleColor = SecondaryIvory // Secondary Ivory
val ConsultationCardBackground = SecondaryIvory // Secondary Ivory

// Dark Theme Colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnBackground = Color(0xFFE0E0E0)
val DarkOnSurface = Color(0xFFE0E0E0)
val White = Color(0xFFFFFFFF)

// Vet/Sitter Theme Colors
val VetCanyon = PrimaryBrown // Primary Brown accent color for vet/sitter
val VetStroke = Color(0xFFE0E0E0) // Border/stroke color
val VetInputBackground = SecondaryIvory // Secondary Ivory input field background