package com.orchestradashboard.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.CommandType
import com.orchestradashboard.shared.ui.agentdetail.AgentDetailViewModel
import com.orchestradashboard.shared.ui.agentdetail.CommandResult
import com.orchestradashboard.shared.ui.agentdetail.DetailTab
import com.orchestradashboard.shared.ui.component.AgentOverviewPanel
import com.orchestradashboard.shared.ui.component.ErrorBanner
import com.orchestradashboard.shared.ui.component.EventFeed
import com.orchestradashboard.shared.ui.component.LoadingOverlay
import com.orchestradashboard.shared.ui.component.PipelineRunList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDetailScreen(
    viewModel: AgentDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAgent() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.agent?.displayName ?: "Agent Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            uiState.error?.let { error ->
                ErrorBanner(message = error, onDismiss = { viewModel.clearError() })
            }

            val tabs = DetailTab.entries
            TabRow(selectedTabIndex = tabs.indexOf(uiState.selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                when (tab) {
                                    DetailTab.OVERVIEW -> "Overview"
                                    DetailTab.PIPELINES -> "Pipelines"
                                    DetailTab.EVENTS -> "Events"
                                },
                            )
                        },
                    )
                }
            }

            when {
                uiState.isLoading -> LoadingOverlay()
                uiState.agent == null -> EmptyAgentState()
                else ->
                    when (uiState.selectedTab) {
                        DetailTab.OVERVIEW -> {
                            AgentOverviewPanel(agent = uiState.agent!!)
                            AgentCommandControls(
                                agent = uiState.agent!!,
                                commandInProgress = uiState.commandInProgress,
                                commandResult = uiState.commandResult,
                                onSendCommand = { viewModel.sendCommand(it) },
                                onDismissResult = { viewModel.clearCommandResult() },
                            )
                        }
                        DetailTab.PIPELINES -> PipelineRunList(pipelineRuns = uiState.pipelineRuns)
                        DetailTab.EVENTS -> EventFeed(events = uiState.events)
                    }
            }
        }
    }
}

@Composable
private fun EmptyAgentState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Agent not found.", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun AgentCommandControls(
    agent: Agent,
    commandInProgress: Boolean,
    commandResult: CommandResult?,
    onSendCommand: (CommandType) -> Unit,
    onDismissResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmingCommand by remember { mutableStateOf<CommandType?>(null) }

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text("Controls", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(
                onClick = { confirmingCommand = CommandType.START },
                enabled = !commandInProgress,
            ) {
                Text("Start")
            }
            OutlinedButton(
                onClick = { confirmingCommand = CommandType.STOP },
                enabled = !commandInProgress,
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Text("Stop")
            }
            OutlinedButton(
                onClick = { confirmingCommand = CommandType.RESTART },
                enabled = !commandInProgress,
            ) {
                Text("Restart")
            }
        }

        if (commandInProgress) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Text(
                    "Sending command...",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        commandResult?.let { result ->
            Spacer(modifier = Modifier.height(8.dp))
            val message =
                when (result) {
                    is CommandResult.Success -> result.message
                    is CommandResult.Failure -> result.message
                }
            val color =
                when (result) {
                    is CommandResult.Success -> MaterialTheme.colorScheme.primary
                    is CommandResult.Failure -> MaterialTheme.colorScheme.error
                }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(message, color = color, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onDismissResult) {
                    Text("Dismiss")
                }
            }
        }
    }

    confirmingCommand?.let { commandType ->
        AlertDialog(
            onDismissRequest = { confirmingCommand = null },
            title = { Text("Confirm ${commandType.name}") },
            text = { Text("Are you sure you want to ${commandType.name.lowercase()} ${agent.displayName}?") },
            confirmButton = {
                Button(onClick = {
                    onSendCommand(commandType)
                    confirmingCommand = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingCommand = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}
