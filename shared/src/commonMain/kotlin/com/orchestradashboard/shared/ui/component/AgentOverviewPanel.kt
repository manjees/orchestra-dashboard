package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Agent

@Composable
fun AgentOverviewPanel(
    agent: Agent,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AgentStatusIndicator(agent.status, size = 16.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = agent.name,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(12.dp))
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
        Spacer(Modifier.height(16.dp))
        val isHealthy = agent.isHealthy
        Surface(
            color = if (isHealthy) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFF44336).copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = if (isHealthy) "Healthy" else "Unhealthy",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = if (isHealthy) Color(0xFF4CAF50) else Color(0xFFF44336),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Last heartbeat: ${formatRelativeTime(agent.lastHeartbeat)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (agent.metadata.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Metadata", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            agent.metadata.forEach { (key, value) ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("$key:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Text(value, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
