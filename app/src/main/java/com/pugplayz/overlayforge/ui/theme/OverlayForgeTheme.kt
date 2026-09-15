package com.pugplayz.overlayforge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF090D12)
val Panel = Color(0xFF101720)
val PanelRaised = Color(0xFF151F2A)
val Border = Color(0xFF263442)
val TextPrimary = Color(0xFFF2F7FA)
val TextMuted = Color(0xFF91A0AE)
val ForgeCyan = Color(0xFF35D6E8)
val ForgeMint = Color(0xFF66F0B3)
val Danger = Color(0xFFFF6B7D)
val Warning = Color(0xFFFFC857)

private val ForgeScheme = darkColorScheme(
    primary = ForgeCyan,
    onPrimary = Color(0xFF001418),
    primaryContainer = Color(0xFF12343B),
    onPrimaryContainer = Color(0xFFC8FAFF),
    secondary = ForgeMint,
    onSecondary = Color(0xFF001A10),
    background = Ink,
    onBackground = TextPrimary,
    surface = Panel,
    onSurface = TextPrimary,
    surfaceVariant = PanelRaised,
    onSurfaceVariant = TextMuted,
    outline = Border,
    error = Danger
)

@Composable
fun OverlayForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ForgeScheme,
        content = content
    )
}
