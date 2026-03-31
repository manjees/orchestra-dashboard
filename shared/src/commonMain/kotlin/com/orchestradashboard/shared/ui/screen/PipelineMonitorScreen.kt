package com.orchestradashboard.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.ConnectionStatus
import com.orchestradashboard.shared.ui.component.ApprovalDialog
import com.orchestradashboard.shared.ui.component.ErrorBanner
import com.orchestradashboard.shared.ui.component.LiveLogPanel
import com.orchestradashboard.shared.ui.component.LoadingOverlay
import com.orchestradashboard.shared.ui.component.ParallelPipelineView
import com.orchestradashboard.shared.ui.component.StepTimeline
import com.orchestradashboard.shared.ui.pipelinemonitor.PipelineMonitorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineMonitorScreen(
    viewModel: PipelineMonitorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPipeline()
        viewModel.startObserving()
    }

    uiState.pendingApproval?.let { approval ->
        ApprovalDialog(
            approval = approval,
            onDismiss = { viewModel.dismissApproval() },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text =
                                uiState.pipeline?.let {
                                    "${it.projectName} #${it.issueNum}"
                                } ?: "Pipeline Monitor",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        uiState.currentStepName?.let { stepName ->
                            Text(
                                text = stepName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.error?.let { error ->
                ErrorBanner(message = error, onDismiss = { viewModel.clearError() })
            }

            if (uiState.connectionStatus == ConnectionStatus.DISCONNECTED && uiState.pipeline != null) {
                ErrorBanner(
                    message = "Connection lost. Data may be stale.",
                    onDismiss = {},
                )
            }

            when {
                uiState.isLoading -> LoadingOverlay()
                uiState.isParallelView -> {
                    ParallelPipelineView(
                        pipelines = uiState.parallelPipelines,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                uiState.pipeline != null -> {
                    StepTimeline(
                        steps = uiState.pipeline!!.steps,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            LiveLogPanel(
                logLines = uiState.logLines,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
            )
        }
    }
}
