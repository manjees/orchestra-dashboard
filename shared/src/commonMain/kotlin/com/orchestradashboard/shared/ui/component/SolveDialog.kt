package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.SolveMode

private val solveModes = listOf(SolveMode.EXPRESS, SolveMode.STANDARD, SolveMode.FULL, SolveMode.AUTO)

private fun SolveMode.label(): String = when (this) {
    SolveMode.EXPRESS -> "Express"
    SolveMode.STANDARD -> "Standard"
    SolveMode.FULL -> "Full"
    SolveMode.AUTO -> "Auto"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolveDialog(
    issues: List<Issue>,
    selectedIssues: Set<Int>,
    solveMode: SolveMode,
    isParallel: Boolean,
    isSolving: Boolean,
    solveError: String?,
    onToggleIssue: (Int) -> Unit,
    onModeChange: (SolveMode) -> Unit,
    onToggleParallel: () -> Unit,
    onSolve: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solve Issues") },
        text = {
            Column {
                // Issue checkboxes
                Text(
                    text = "Select Issues",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(issues, key = { it.number }) { issue ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleIssue(issue.number) },
                        ) {
                            Checkbox(
                                checked = selectedIssues.contains(issue.number),
                                onCheckedChange = { onToggleIssue(issue.number) },
                            )
                            Text(
                                text = "#${issue.number} ${issue.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Mode selector
                Text(
                    text = "Mode",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    solveModes.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = solveMode == mode,
                            onClick = { onModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = solveModes.size),
                        ) {
                            Text(mode.label())
                        }
                    }
                }

                // Parallel toggle (only when multiple issues selected)
                if (selectedIssues.size > 1) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parallel_toggle"),
                    ) {
                        Text(
                            text = "Run in Parallel",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Switch(
                            checked = isParallel,
                            onCheckedChange = { onToggleParallel() },
                        )
                    }
                }

                // Error message
                if (solveError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = solveError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSolve,
                enabled = selectedIssues.isNotEmpty() && !isSolving,
            ) {
                if (isSolving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp).testTag("solve_loading_indicator"),
                        strokeWidth = 2.dp,
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Solve")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
