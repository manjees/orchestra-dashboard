package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.DesignResult
import com.orchestradashboard.shared.domain.model.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignPanel(
    projects: List<Project>,
    selectedProject: Project?,
    figmaUrl: String,
    isLoading: Boolean,
    result: DesignResult?,
    onProjectSelect: (Project) -> Unit,
    onFigmaUrlChange: (String) -> Unit,
    onExecute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selectedProject?.name ?: "Select project",
                onValueChange = {},
                readOnly = true,
                label = { Text("Project") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                projects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.name) },
                        onClick = {
                            onProjectSelect(project)
                            expanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = figmaUrl,
            onValueChange = onFigmaUrlChange,
            label = { Text("Figma URL") },
            placeholder = { Text("https://figma.com/file/...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onExecute,
            enabled = selectedProject != null && figmaUrl.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Analyzing..." else "Analyze Design")
        }
        result?.let { designResult ->
            Spacer(modifier = Modifier.height(12.dp))
            Text("UI Spec", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(designResult.spec, style = MaterialTheme.typography.bodyMedium)
            if (designResult.suggestedIssues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("Create ${designResult.suggestedIssues.size} Issues")
                }
            }
        }
    }
}
