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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.DiscussResult
import com.orchestradashboard.shared.domain.model.PlannedIssue
import com.orchestradashboard.shared.domain.model.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussPanel(
    projects: List<Project>,
    selectedProject: Project?,
    question: String,
    isLoading: Boolean,
    result: DiscussResult?,
    onProjectSelect: (Project) -> Unit,
    onQuestionChange: (String) -> Unit,
    onExecute: () -> Unit,
    onConvertToIssue: (PlannedIssue) -> Unit,
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
            value = question,
            onValueChange = onQuestionChange,
            label = { Text("Question") },
            placeholder = { Text("What should I implement first?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onExecute,
            enabled = selectedProject != null && question.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Asking..." else "Ask")
        }
        result?.let { discussResult ->
            Spacer(modifier = Modifier.height(12.dp))
            Text("Answer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(discussResult.answer, style = MaterialTheme.typography.bodyMedium)
            if (discussResult.suggestedIssues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Suggested Issues",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                discussResult.suggestedIssues.forEach { issue ->
                    Spacer(modifier = Modifier.height(4.dp))
                    SuggestedIssueRow(issue, onConvertToIssue = onConvertToIssue)
                }
            }
        }
    }
}

@Composable
private fun SuggestedIssueRow(
    issue: PlannedIssue,
    onConvertToIssue: (PlannedIssue) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(issue.title, style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = { onConvertToIssue(issue) }, modifier = Modifier.height(32.dp)) {
            Text("Convert to Issue", style = MaterialTheme.typography.labelSmall)
        }
    }
}
