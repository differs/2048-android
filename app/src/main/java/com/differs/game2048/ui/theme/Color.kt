package com.differs.game2048.ui.theme

import androidx.compose.ui.graphics.Color

// Brand / board palette (inspired by the classic 2048 look, modernized).
val Warm50 = Color(0xFFFAF8EF)
val Warm100 = Color(0xFFF2EFE4)
val BoardBg = Color(0xFFBBADA0)
val EmptyCell = Color(0xFFCDC1B4)
val TextDark = Color(0xFF776E65)
val TextLight = Color(0xFFF9F6F2)
val Accent = Color(0xFFEDC22E)
val AccentDark = Color(0xFFD4AF10)

// Dark theme surfaces.
val DarkBg = Color(0xFF14110E)
val DarkBoardBg = Color(0xFF3A342E)
val DarkEmptyCell = Color(0xFF4A423B)
val DarkSurface = Color(0xFF201C18)

/** Returns background + text color for a tile of [value]. */
fun tileColors(value: Int, dark: Boolean): Pair<Color, Color> {
    val bg = when (value) {
        2 -> Color(0xFFEEE4DA)
        4 -> Color(0xFFEDE0C8)
        8 -> Color(0xFFF2B179)
        16 -> Color(0xFFF59563)
        32 -> Color(0xFFF67C5F)
        64 -> Color(0xFFF65E3B)
        128 -> Color(0xFFEDCF72)
        256 -> Color(0xFFEDCC61)
        512 -> Color(0xFFEDC850)
        1024 -> Color(0xFFEDC53F)
        2048 -> Color(0xFFEDC22E)
        else -> Color(0xFF3C3A32) // super tiles (>2048)
    }
    val fg = if (value <= 4) TextDark else TextLight
    return if (dark && value <= 4) {
        // Slightly deepen low tiles in dark mode for contrast.
        bg.copy(alpha = 0.92f) to TextDark
    } else {
        bg to fg
    }
}
