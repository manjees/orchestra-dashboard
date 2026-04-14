package com.orchestradashboard.shared.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.orchestradashboard.shared.ui.commandcenter.CommandCenterViewModel
import com.orchestradashboard.shared.ui.commandcenter.CommandTab
import com.orchestradashboard.shared.ui.component.DesignPanel
import com.orchestradashboard.shared.ui.component.DiscussPanel
import com.orchestradashboard.shared.ui.component.ErrorBanner
import com.orchestradashboard.shared.ui.component.InitProjectForm
import com.orchestradashboard.shared.ui.component.PlanIssuesPanel
import com.orchestradashboard.shared.ui.component.ShellPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterScreen(
    viewModel: CommandCenterViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    val tabs = listOf("Init", "Plan", "Discuss", "Design", "Shell")
    val tabValues = CommandTab.entries.toTypedArray()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Command Center") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            state.error?.let { error ->
                ErrorBanner(message = error, onDismiss = { viewModel.clearError() })
            }

            ScrollableTabRow(selectedTabIndex = state.activeTab.ordinal) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.activeTab.ordinal == index,
                        onClick = { viewModel.selectTab(tabValues[index]) },
                        text = { Text(title) },
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                when (state.activeTab) {
                    CommandTab.INIT ->
                        InitProjectForm(
                            name = state.initName,
                            description = state.initDescription,
                            visibility = state.initVisibility,
                            isLoading = state.isInitLoading,
                            result = state.initResult,
                            onNameChange = viewModel::updateInitName,
                            onDescriptionChange = viewModel::updateInitDescription,
                            onVisibilityChange = viewModel::updateInitVisibility,
                            onExecute = viewModel::executeInit,
                        )
                    CommandTab.PLAN ->
                        PlanIssuesPanel(
                            projects = state.projects,
                            selectedProject = state.planSelectedProject,
                            isLoading = state.isPlanLoading,
                            result = state.planResult,
                            onProjectSelect = viewModel::selectPlanProject,
                            onExecute = viewModel::executePlan,
                        )
                    CommandTab.DISCUSS ->
                        DiscussPanel(
                            projects = state.projects,
                            selectedProject = state.discussSelectedProject,
                            question = state.discussQuestion,
                            isLoading = state.isDiscussLoading,
                            result = state.discussResult,
                            onProjectSelect = viewModel::selectDiscussProject,
                            onQuestionChange = viewModel::updateDiscussQuestion,
                            onExecute = viewModel::executeDiscuss,
                            onConvertToIssue = viewModel::convertSuggestedIssue,
                        )
                    CommandTab.DESIGN ->
                        DesignPanel(
                            projects = state.projects,
                            selectedProject = state.designSelectedProject,
                            figmaUrl = state.designFigmaUrl,
                            isLoading = state.isDesignLoading,
                            result = state.designResult,
                            onProjectSelect = viewModel::selectDesignProject,
                            onFigmaUrlChange = viewModel::updateDesignFigmaUrl,
                            onExecute = viewModel::executeDesign,
                        )
                    CommandTab.SHELL ->
                        ShellPanel(
                            command = state.shellCommand,
                            isLoading = state.isShellLoading,
                            result = state.shellResult,
                            showDangerDialog = state.showDangerDialog,
                            pendingDangerousCommand = state.pendingDangerousCommand,
                            onCommandChange = viewModel::updateShellCommand,
                            onExecute = viewModel::executeShell,
                            onConfirmDanger = viewModel::confirmDangerousCommand,
                            onCancelDanger = viewModel::cancelDangerousCommand,
                        )
                }
            }
        }
    }
}
