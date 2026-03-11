package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Agent

@Composable
fun AgentOverviewPanel(
    agent: Agent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AgentStatusIndicator(agent.status, size = 16.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(agent.name, style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(Modifier.height(8.dp))
                OverviewRow(label = "ID", value = agent.id)
                OverviewRow(label = "Type", value = agent.type.name.lowercase())
                OverviewRow(label = "Status", value = agent.status.name)
                OverviewRow(label = "Last Heartbeat", value = formatRelativeTime(agent.lastHeartbeat))
                OverviewRow(
                    label = "Health",
                    value = if (agent.isHealthy) "Healthy" else "Unhealthy",
                )
            }
        }

        if (agent.metadata.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Metadata", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    agent.metadata.forEach { (key, value) ->
                        OverviewRow(label = key, value = value)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
