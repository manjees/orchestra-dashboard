package com.orchestradashboard.shared.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class StatusColors(
    val running: Color = Color(0xFF4CAF50),
    val success: Color = Color(0xFF4CAF50),
    val idle: Color = Color(0xFF2196F3),
    val error: Color = Color(0xFFF44336),
    val offline: Color = Color(0xFF9E9E9E),
)

val LocalStatusColors = staticCompositionLocalOf { StatusColors() }
