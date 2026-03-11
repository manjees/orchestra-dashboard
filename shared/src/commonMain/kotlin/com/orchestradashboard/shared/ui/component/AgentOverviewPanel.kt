package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.ui.theme.DashboardTheme

@Composable
fun AgentOverviewPanel(
    agent: Agent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AgentStatusIndicator(agent.status, size = 16.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = agent.name,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = agent.type.name.lowercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            HealthBadge(isHealthy = agent.isHealthy)
        }

        InfoRow(label = "Status", value = agent.status.name)
        InfoRow(label = "Last seen", value = formatRelativeTime(agent.lastHeartbeat))

        if (agent.metadata.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Metadata",
                style = MaterialTheme.typography.titleSmall,
            )
            agent.metadata.forEach { (key, value) ->
                InfoRow(label = key, value = value)
            }
        }
    }
}

@Composable
private fun HealthBadge(
    isHealthy: Boolean,
    modifier: Modifier = Modifier,
) {
    val color =
        if (isHealthy) {
            DashboardTheme.statusColors.running
        } else {
            DashboardTheme.statusColors.error
        }
    val label = if (isHealthy) "Healthy" else "Unhealthy"

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
