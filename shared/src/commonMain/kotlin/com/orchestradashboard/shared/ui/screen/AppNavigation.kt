package com.orchestradashboard.shared.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.ui.agentdetail.AgentDetailViewModel
import com.orchestradashboard.shared.ui.commandcenter.CommandCenterViewModel
import com.orchestradashboard.shared.ui.dashboardhome.DashboardHomeViewModel
import com.orchestradashboard.shared.ui.pipelinemonitor.PipelineMonitorViewModel
import com.orchestradashboard.shared.ui.projectexplorer.ProjectExplorerViewModel
import com.orchestradashboard.shared.ui.settings.SettingsViewModel
import com.orchestradashboard.shared.ui.solvedialog.SolveDialogViewModel

sealed class Screen {
    data object DashboardHome : Screen()

    data object Dashboard : Screen()

    data class AgentDetail(val agentId: String) : Screen()

    data object ProjectExplorer : Screen()

    data class PipelineMonitor(val pipelineId: String) : Screen()

    data object CommandCenter : Screen()

    data object Settings : Screen()
}

@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    dashboardHomeViewModelFactory: () -> DashboardHomeViewModel,
    agentDetailViewModelFactory: (String) -> AgentDetailViewModel,
    projectExplorerViewModelFactory: () -> ProjectExplorerViewModel,
    solveDialogViewModelFactory: () -> SolveDialogViewModel,
    pipelineMonitorViewModelFactory: (String) -> PipelineMonitorViewModel,
    commandCenterViewModelFactory: () -> CommandCenterViewModel,
    settingsViewModelFactory: () -> SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    var currentScreen: Screen by remember { mutableStateOf(Screen.DashboardHome) }

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
            DisposableEffect(screen.pipelineId) {
                onDispose { vm.onCleared() }
            }
            PipelineMonitorScreen(
                viewModel = vm,
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
    }
}
