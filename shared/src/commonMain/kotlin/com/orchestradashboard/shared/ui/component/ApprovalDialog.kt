package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.ApprovalRequest

private const val SECONDS_PER_MINUTE = 60

@Composable
fun ApprovalDialog(
    approval: ApprovalRequest,
    remainingTimeSec: Int?,
    isTimedOut: Boolean,
    onRespond: (decision: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(buildTitle(approval.approvalType))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Context information
                ApprovalContextSection(approval)

                // Timeout countdown
                CountdownSection(remainingTimeSec, approval.timeoutSec, isTimedOut)

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons based on approval type
                if (isTimedOut) {
                    Text(
                        text = "Auto-approved: timeout reached. The server has automatically approved this request.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    ApprovalActionButtons(
                        approvalType = approval.approvalType,
                        onRespond = onRespond,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        },
    )
}

@Composable
private fun ApprovalContextSection(approval: ApprovalRequest) {
    Text(
        text = "This step requires ${approval.approvalType} approval.",
        style = MaterialTheme.typography.bodyMedium,
    )
    approval.context?.let { ctx ->
        ctx.eta?.let { eta ->
            Text(
                text = "ETA: $eta",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ctx.splitProposal?.let { proposal ->
            Text(
                text = "Split Proposal: $proposal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ctx.detail?.let { detail ->
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CountdownSection(
    remainingTimeSec: Int?,
    timeoutSec: Int,
    isTimedOut: Boolean,
) {
    if (remainingTimeSec == null) return

    val progress = if (timeoutSec > 0) remainingTimeSec.toFloat() / timeoutSec else 0f
    val minutes = remainingTimeSec / SECONDS_PER_MINUTE
    val seconds = remainingTimeSec % SECONDS_PER_MINUTE

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color =
                if (isTimedOut) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
        )
        Text(
            text = if (isTimedOut) "Timed out" else "$minutes:${seconds.toString().padStart(2, '0')} remaining",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
        )
    }
}

@Composable
private fun ApprovalActionButtons(
    approvalType: String,
    onRespond: (String) -> Unit,
) {
    when (approvalType) {
        "strategy" -> StrategyButtons(onRespond)
        "supreme_court" -> SupremeCourtButtons(onRespond)
        else -> GenericButtons(onRespond)
    }
}

@Composable
private fun StrategyButtons(onRespond: (String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = { onRespond("split_execute") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Split & Execute")
        }
        OutlinedButton(
            onClick = { onRespond("no_split") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("No Split (Full)")
        }
        TextButton(
            onClick = { onRespond("cancel") },
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
        ) {
            Text("Cancel")
        }
    }
}

@Composable
private fun SupremeCourtButtons(onRespond: (String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = { onRespond("uphold") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Uphold")
        }
        OutlinedButton(
            onClick = { onRespond("overturn") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Overturn")
        }
        OutlinedButton(
            onClick = { onRespond("redesign") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Redesign")
        }
    }
}

@Composable
private fun GenericButtons(onRespond: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = { onRespond("approve") },
            modifier = Modifier.weight(1f),
        ) {
            Text("Approve")
        }
        OutlinedButton(
            onClick = { onRespond("reject") },
            modifier = Modifier.weight(1f),
        ) {
            Text("Reject")
        }
    }
}

private fun buildTitle(approvalType: String): String =
    when (approvalType) {
        "strategy" -> "Strategy Approval Required"
        "supreme_court" -> "Supreme Court Review Required"
        else -> "Approval Required: $approvalType"
    }
