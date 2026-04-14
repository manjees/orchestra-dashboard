package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.ShellResult

@Composable
fun ShellPanel(
    command: String,
    isLoading: Boolean,
    result: ShellResult?,
    showDangerDialog: Boolean,
    pendingDangerousCommand: String?,
    onCommandChange: (String) -> Unit,
    onExecute: () -> Unit,
    onConfirmDanger: () -> Unit,
    onCancelDanger: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Warning: Shell commands execute directly on the orchestrator host.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = command,
            onValueChange = onCommandChange,
            label = { Text("Command") },
            placeholder = { Text("ls -la") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onExecute,
            enabled = command.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Executing..." else "Execute")
        }
        result?.let { shellResult ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Exit code: ${shellResult.exitCode}",
                style = MaterialTheme.typography.labelSmall,
                color =
                    if (shellResult.exitCode == 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
            Spacer(modifier = Modifier.height(4.dp))
            SelectionContainer {
                Text(
                    text = shellResult.output,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState()),
                )
            }
        }
    }

    if (showDangerDialog && pendingDangerousCommand != null) {
        DangerCommandDialog(
            command = pendingDangerousCommand,
            onConfirm = onConfirmDanger,
            onDismiss = onCancelDanger,
        )
    }
}
