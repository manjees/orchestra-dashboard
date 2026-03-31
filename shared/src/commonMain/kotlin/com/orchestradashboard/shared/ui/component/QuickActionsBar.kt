package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuickActionsBar(
    onNewSolveClick: () -> Unit,
    onViewProjectsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FilledTonalButton(
            onClick = onNewSolveClick,
            modifier = Modifier.weight(1f),
        ) {
            Text("New Solve")
        }
        FilledTonalButton(
            onClick = onViewProjectsClick,
            modifier = Modifier.weight(1f),
        ) {
            Text("View Projects")
        }
    }
}
