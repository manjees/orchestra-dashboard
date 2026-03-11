package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.ui.theme.DashboardTheme

@Composable
fun AgentStatusIndicator(
    status: Agent.AgentStatus,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
) {
    val color =
        when (status) {
            Agent.AgentStatus.RUNNING -> DashboardTheme.statusColors.running
            Agent.AgentStatus.IDLE -> DashboardTheme.statusColors.idle
            Agent.AgentStatus.ERROR -> DashboardTheme.statusColors.error
            Agent.AgentStatus.OFFLINE -> DashboardTheme.statusColors.offline
        }
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(color),
    )
}
