package com.orchestradashboard.desktop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.orchestradashboard.shared.domain.model.Agent

object StatusColors {
    val running = Color(0xFF4CAF50)
    val idle = Color(0xFF2196F3)
    val error = Color(0xFFF44336)
    val offline = Color(0xFF9E9E9E)
}

fun Agent.AgentStatus.toColor(): Color =
    when (this) {
        Agent.AgentStatus.RUNNING -> StatusColors.running
        Agent.AgentStatus.IDLE -> StatusColors.idle
        Agent.AgentStatus.ERROR -> StatusColors.error
        Agent.AgentStatus.OFFLINE -> StatusColors.offline
    }

@Composable
fun DashboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}
