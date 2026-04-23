package com.orchestradashboard.desktop

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.orchestradashboard.desktop.di.AppContainer
import com.orchestradashboard.shared.ui.screen.AppNavigation
import com.orchestradashboard.shared.ui.theme.DashboardTheme

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Orchestra Dashboard",
        ) {
            val viewModel = remember { AppContainer.createDashboardViewModel() }

            DisposableEffect(viewModel) {
                onDispose { viewModel.onCleared() }
            }

            DashboardTheme {
                AppNavigation(
                    dashboardViewModel = viewModel,
                    dashboardHomeViewModelFactory = {
                        AppContainer.createDashboardHomeViewModel()
                    },
                    agentDetailViewModelFactory = { agentId ->
                        AppContainer.createAgentDetailViewModel(agentId)
                    },
                    projectExplorerViewModelFactory = {
                        AppContainer.createProjectExplorerViewModel()
                    },
                    solveDialogViewModelFactory = {
                        AppContainer.createSolveDialogViewModel()
                    },
                    pipelineMonitorViewModelFactory = { pipelineId ->
                        AppContainer.createPipelineMonitorViewModel(pipelineId)
                    },
                    commandCenterViewModelFactory = {
                        AppContainer.createCommandCenterViewModel()
                    },
                    settingsViewModelFactory = {
                        AppContainer.createSettingsViewModel()
                    },
                    historyViewModelFactory = {
                        AppContainer.createHistoryViewModel()
                    },
                    analyticsViewModelFactory = {
                        AppContainer.createAnalyticsViewModel()
                    },
                )
            }
        }
    }
