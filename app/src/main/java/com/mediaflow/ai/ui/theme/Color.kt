package com.mediaflow.ai.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Brand Gradient (Purple to Electric Blue)
val PurplePrimary = Color(0xFF7C3AED)
val PurpleDark = Color(0xFF6D28D9)
val PurpleLight = Color(0xFFA78BFA)
val BlueAccent = Color(0xFF2563EB)
val BlueLight = Color(0xFF60A5FA)
val CyanAccent = Color(0xFF06B6D4)

val BrandGradient = Brush.linearGradient(
    colors = listOf(PurplePrimary, BlueAccent)
)

val FrostedGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x33FFFFFF),
        Color(0x0DFFFFFF)
    )
)

val DarkFrostedGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x331E293B),
        Color(0x1A0F172A)
    )
)

// Light Theme Palettes
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)
val LightBorder = Color(0xFFE2E8F0)

// Dark Theme Palettes
val DarkBackground = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF131B2E)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkBorder = Color(0xFF334155)

// Status & Accents
val SuccessGreen = Color(0xFF10B981)
val FavoriteRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)
