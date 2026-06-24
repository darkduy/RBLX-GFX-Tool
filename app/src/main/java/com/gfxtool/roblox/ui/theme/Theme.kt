package com.gfxtool.roblox.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Palette ───────────────────────────────────────────────────────
// Deep charcoal base, muted surfaces, single vivid accent (Roblox red-ish → coral glow)
val BackgroundDark   = Color(0xFF0E0E10)
val SurfaceDark      = Color(0xFF1A1A1F)
val SurfaceVariant   = Color(0xFF232329)
val Outline          = Color(0xFF2E2E36)
val AccentPrimary    = Color(0xFFFF4154)   // vivid coral-red
val AccentSecondary  = Color(0xFFFF8C42)   // warm amber for sliders
val OnSurface        = Color(0xFFEEEEF2)
val OnSurfaceMuted   = Color(0xFF888896)

private val DarkColorScheme = darkColorScheme(
    primary          = AccentPrimary,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFF3A0A10),
    secondary        = AccentSecondary,
    background       = BackgroundDark,
    surface          = SurfaceDark,
    surfaceVariant   = SurfaceVariant,
    onBackground     = OnSurface,
    onSurface        = OnSurface,
    onSurfaceVariant = OnSurfaceMuted,
    outline          = Outline,
    error            = Color(0xFFFF5370),
)

@Composable
fun GfxToolTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content,
    )
}
