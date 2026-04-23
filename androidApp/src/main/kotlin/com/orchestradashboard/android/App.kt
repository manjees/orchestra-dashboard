package com.orchestradashboard.android

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.orchestradashboard.android.di.AppContainer
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.ui.screen.AppNavigation
import com.orchestradashboard.shared.ui.theme.DashboardTheme

class OrchestraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = AppContainer.createDashboardViewModel()

        setContent {
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onCleared()
    }
}
