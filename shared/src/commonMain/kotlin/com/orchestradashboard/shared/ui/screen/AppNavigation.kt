package com.orchestradashboard.shared.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.ui.agentdetail.AgentDetailViewModel
import com.orchestradashboard.shared.ui.analytics.AnalyticsViewModel
import com.orchestradashboard.shared.ui.commandcenter.CommandCenterViewModel
import com.orchestradashboard.shared.ui.dashboardhome.DashboardHomeViewModel
import com.orchestradashboard.shared.ui.history.HistoryViewModel
import com.orchestradashboard.shared.ui.logstream.LogStreamViewModel
import com.orchestradashboard.shared.ui.pipelinemonitor.PipelineMonitorViewModel
import com.orchestradashboard.shared.ui.projectexplorer.ProjectExplorerViewModel
import com.orchestradashboard.shared.ui.settings.SettingsViewModel
import com.orchestradashboard.shared.ui.solvedialog.SolveDialogViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed class Screen {
    data object DashboardHome : Screen()

    data object Dashboard : Screen()

    data class AgentDetail(val agentId: String) : Screen()

    data object ProjectExplorer : Screen()

    data class PipelineMonitor(val pipelineId: String) : Screen()

    data object CommandCenter : Screen()

    data object Settings : Screen()

    data object History : Screen()

    data object Analytics : Screen()
}

@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    dashboardHomeViewModelFactory: () -> DashboardHomeViewModel,
    agentDetailViewModelFactory: (String) -> AgentDetailViewModel,
    projectExplorerViewModelFactory: () -> ProjectExplorerViewModel,
    solveDialogViewModelFactory: () -> SolveDialogViewModel,
    pipelineMonitorViewModelFactory: (String) -> PipelineMonitorViewModel,
    logStreamViewModelFactory: () -> LogStreamViewModel,
    commandCenterViewModelFactory: () -> CommandCenterViewModel,
    settingsViewModelFactory: () -> SettingsViewModel,
    historyViewModelFactory: () -> HistoryViewModel,
    analyticsViewModelFactory: () -> AnalyticsViewModel,
    modifier: Modifier = Modifier,
    initialPipelineId: String? = null,
    deepLinkPipelineIds: Flow<String> = emptyFlow(),
) {
    var currentScreen: Screen by remember {
        mutableStateOf(
            if (initialPipelineId != null) {
                Screen.PipelineMonitor(initialPipelineId)
            } else {
                Screen.DashboardHome
            },
        )
    }

    LaunchedEffect(deepLinkPipelineIds) {
        deepLinkPipelineIds.collect { pipelineId ->
            currentScreen = Screen.PipelineMonitor(pipelineId)
        }
    }

    when (val screen = currentScreen) {
        is Screen.DashboardHome -> {
            val vm = remember { dashboardHomeViewModelFactory() }
            DisposableEffect(Unit) {
                onDispose { vm.onCleared() }
            }
            DashboardHomeScreen(
                viewModel = vm,
                onNewSolveClick = { currentScreen = Screen.ProjectExplorer },
                onViewProjectsClick = { currentScreen = Screen.ProjectExplorer },
                onCommandCenterClick = { currentScreen = Screen.CommandCenter },
                onPipelineClick = { pipelineId -> currentScreen = Screen.PipelineMonitor(pipelineId) },
                onSettingsClick = { currentScreen = Screen.Settings },
                onHistoryClick = { currentScreen = Screen.History },
                onAnalyticsClick = { currentScreen = Screen.Analytics },
                modifier = modifier,
            )
        }
        is Screen.Dashboard ->
            DashboardScreen(
                viewModel = dashboardViewModel,
                onAgentClick = { agentId -> currentScreen = Screen.AgentDetail(agentId) },
                onViewProjectsClick = { currentScreen = Screen.ProjectExplorer },
                modifier = modifier,
            )
        is Screen.AgentDetail -> {
            val vm = remember(screen.agentId) { agentDetailViewModelFactory(screen.agentId) }
            DisposableEffect(screen.agentId) {
                onDispose { vm.onCleared() }
            }
            AgentDetailScreen(
                viewModel = vm,
                onBackClick = { currentScreen = Screen.DashboardHome },
                modifier = modifier,
            )
        }
        is Screen.ProjectExplorer -> {
            val vm = remember { projectExplorerViewModelFactory() }
            val solveVm = remember { solveDialogViewModelFactory() }
            DisposableEffect(Unit) {
                onDispose {
                    vm.onCleared()
                    solveVm.onCleared()
                }
            }
            ProjectExplorerScreen(
                viewModel = vm,
                solveDialogViewModel = solveVm,
                onBackClick = { currentScreen = Screen.DashboardHome },
                onNavigateToPipeline = { pipelineId -> currentScreen = Screen.PipelineMonitor(pipelineId) },
                modifier = modifier,
            )
        }
        is Screen.PipelineMonitor -> {
            val vm =
                remember(screen.pipelineId) {
                    pipelineMonitorViewModelFactory(screen.pipelineId)
                }
            val logVm =
                remember(screen.pipelineId) {
                    logStreamViewModelFactory()
                }
            DisposableEffect(screen.pipelineId) {
                onDispose {
                    vm.onCleared()
                    logVm.onCleared()
                }
            }
            PipelineMonitorScreen(
                viewModel = vm,
                logStreamViewModel = logVm,
                onBackClick = { currentScreen = Screen.DashboardHome },
                modifier = modifier,
            )
        }
        is Screen.CommandCenter -> {
            val vm = remember { commandCenterViewModelFactory() }
            DisposableEffect(Unit) {
                onDispose { vm.onCleared() }
            }
            CommandCenterScreen(
                viewModel = vm,
                onBackClick = { currentScreen = Screen.DashboardHome },
                modifier = modifier,
            )
        }
        is Screen.Settings -> {
            val vm = remember { settingsViewModelFactory() }
            DisposableEffect(Unit) {
                onDispose { vm.onCleared() }
            }
            SettingsScreen(
                viewModel = vm,
                onBackClick = { currentScreen = Screen.DashboardHome },
                modifier = modifier,
            )
        }
        is Screen.History -> {
            val vm = remember { historyViewModelFactory() }
            DisposableEffect(Unit) {
                onDispose { vm.onCleared() }
            }
            HistoryScreen(
                viewModel = vm,
                onBackClick = { currentScreen = Screen.DashboardHome },
                onAnalyticsClick = { currentScreen = Screen.Analytics },
                modifier = modifier,
            )
        }
        is Screen.Analytics -> {
            val vm = remember { analyticsViewModelFactory() }
            DisposableEffect(Unit) {
                onDispose { vm.onCleared() }
            }
            AnalyticsScreen(
                viewModel = vm,
                onBackClick = { currentScreen = Screen.DashboardHome },
                modifier = modifier,
            )
        }
    }
}
