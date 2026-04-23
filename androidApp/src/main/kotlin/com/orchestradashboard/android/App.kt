package com.orchestradashboard.android

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.orchestradashboard.android.di.AppContainer
import com.orchestradashboard.android.push.PushNotificationSetup
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.ui.screen.AppNavigation
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.coroutines.flow.MutableSharedFlow

class OrchestraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
        PushNotificationSetup.initialize(
            context = this,
            notificationRepository = AppContainer.notificationRepository,
            pushProvider = AppContainer.pushNotificationProvider,
        )
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: DashboardViewModel
    private val deepLinkFlow = MutableSharedFlow<String>(extraBufferCapacity = 4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = AppContainer.createDashboardViewModel()

        val initialPipelineId =
            intent?.getStringExtra(PushNotificationSetup.EXTRA_PIPELINE_ID)

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
                    initialPipelineId = initialPipelineId,
                    deepLinkPipelineIds = deepLinkFlow,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(PushNotificationSetup.EXTRA_PIPELINE_ID)?.let { pipelineId ->
            deepLinkFlow.tryEmit(pipelineId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onCleared()
    }
}
