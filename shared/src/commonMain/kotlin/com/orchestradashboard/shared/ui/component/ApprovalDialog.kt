package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.ApprovalDecision
import com.orchestradashboard.shared.domain.model.ApprovalRequest
import com.orchestradashboard.shared.domain.model.GenericDecision
import com.orchestradashboard.shared.domain.model.StrategyDecision
import com.orchestradashboard.shared.domain.model.SupremeCourtDecision

private const val SECONDS_PER_MINUTE = 60

@Composable
fun ApprovalDialog(
    approval: ApprovalRequest,
    remainingTimeSec: Int?,
    isTimedOut: Boolean,
    isSubmitting: Boolean,
    error: String?,
    onRespond: (ApprovalDecision) -> Unit,
    onDismiss: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(buildDialogTitle(approval.approvalType))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Context information
                ApprovalContextSection(approval)

                // Timeout countdown
                CountdownSection(remainingTimeSec, approval.timeoutSec, isTimedOut)

                // Error display
                error?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(onClick = onClearError) {
                        Text("Dismiss error")
                    }
                }

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
                        enabled = !isSubmitting,
                    )
                }
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp).size(16.dp),
                        strokeWidth = 2.dp,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
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

    val progress = calculateProgress(remainingTimeSec, timeoutSec)

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
            text = formatCountdownText(remainingTimeSec, isTimedOut),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
        )
    }
}

@Composable
private fun ApprovalActionButtons(
    approvalType: String,
    onRespond: (ApprovalDecision) -> Unit,
    enabled: Boolean,
) {
    when (approvalType) {
        "strategy" -> StrategyButtons(onRespond, enabled)
        "supreme_court" -> SupremeCourtButtons(onRespond, enabled)
        else -> GenericButtons(onRespond, enabled)
    }
}

@Composable
private fun StrategyButtons(
    onRespond: (ApprovalDecision) -> Unit,
    enabled: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = { onRespond(StrategyDecision.SplitExecute) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Text("Split & Execute")
        }
        OutlinedButton(
            onClick = { onRespond(StrategyDecision.NoSplit) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Text("No Split (Full)")
        }
        TextButton(
            onClick = { onRespond(StrategyDecision.Cancel) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
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
private fun SupremeCourtButtons(
    onRespond: (ApprovalDecision) -> Unit,
    enabled: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = { onRespond(SupremeCourtDecision.Uphold) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Text("Uphold")
        }
        OutlinedButton(
            onClick = { onRespond(SupremeCourtDecision.Overturn) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Text("Overturn")
        }
        OutlinedButton(
            onClick = { onRespond(SupremeCourtDecision.Redesign) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Text("Redesign")
        }
    }
}

@Composable
private fun GenericButtons(
    onRespond: (ApprovalDecision) -> Unit,
    enabled: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = { onRespond(GenericDecision("approve")) },
            modifier = Modifier.weight(1f),
            enabled = enabled,
        ) {
            Text("Approve")
        }
        OutlinedButton(
            onClick = { onRespond(GenericDecision("reject")) },
            modifier = Modifier.weight(1f),
            enabled = enabled,
        ) {
            Text("Reject")
        }
    }
}

internal fun buildDialogTitle(approvalType: String): String =
    when (approvalType) {
        "strategy" -> "Strategy Approval Required"
        "supreme_court" -> "Supreme Court Review Required"
        else -> "Approval Required: $approvalType"
    }

internal fun formatCountdownText(
    remainingTimeSec: Int,
    isTimedOut: Boolean,
): String {
    if (isTimedOut) return "Timed out"
    val minutes = remainingTimeSec / SECONDS_PER_MINUTE
    val seconds = remainingTimeSec % SECONDS_PER_MINUTE
    return "$minutes:${seconds.toString().padStart(2, '0')} remaining"
}

internal fun calculateProgress(
    remainingTimeSec: Int,
    timeoutSec: Int,
): Float = if (timeoutSec > 0) remainingTimeSec.toFloat() / timeoutSec else 0f

internal fun approvalDecisionsForType(approvalType: String): List<ApprovalDecision> =
    when (approvalType) {
        "strategy" ->
            listOf(
                StrategyDecision.SplitExecute,
                StrategyDecision.NoSplit,
                StrategyDecision.Cancel,
            )
        "supreme_court" ->
            listOf(
                SupremeCourtDecision.Uphold,
                SupremeCourtDecision.Overturn,
                SupremeCourtDecision.Redesign,
            )
        else ->
            listOf(
                GenericDecision("approve"),
                GenericDecision("reject"),
            )
    }
