package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Issue
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IssueRow(
    issue: Issue,
    onSolveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "#${issue.number} ${issue.title}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatIssueDate(issue),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (issue.labels.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        issue.labels.forEach { label ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = onSolveClick) {
            Text("Solve")
        }
    }
}

private fun formatIssueDate(issue: Issue): String {
    val local = issue.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.year}-${local.monthNumber.toString().padStart(2, '0')}-${local.dayOfMonth.toString().padStart(2, '0')}"
}
