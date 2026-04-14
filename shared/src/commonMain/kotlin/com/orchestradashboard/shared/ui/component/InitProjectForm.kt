package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.CommandResult
import com.orchestradashboard.shared.domain.model.ProjectVisibility

@Composable
fun InitProjectForm(
    name: String,
    description: String,
    visibility: ProjectVisibility,
    isLoading: Boolean,
    result: CommandResult?,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onVisibilityChange: (ProjectVisibility) -> Unit,
    onExecute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Project Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Public", modifier = Modifier.weight(1f))
            Switch(
                checked = visibility == ProjectVisibility.PUBLIC,
                onCheckedChange = { checked ->
                    onVisibilityChange(if (checked) ProjectVisibility.PUBLIC else ProjectVisibility.PRIVATE)
                },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onExecute,
            enabled = name.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Creating..." else "Create Project")
        }
        result?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (it.success) "✓ ${it.message}" else "✗ ${it.message}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (it.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
    }
}
