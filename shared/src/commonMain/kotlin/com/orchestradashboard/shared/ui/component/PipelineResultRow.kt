package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.PipelineRunStatus

@Composable
fun PipelineResultRow(
    result: PipelineResult,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isPassed = result.status == PipelineRunStatus.PASSED
    val rowModifier =
        if (onClick != null) {
            modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp)
        } else {
            modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = if (isPassed) "Passed" else "Failed",
            tint = if (isPassed) Color(0xFF43A047) else Color(0xFFE53935),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = result.projectName,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "#${result.issueNum}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = formatElapsed(result.elapsedTotalSec),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
