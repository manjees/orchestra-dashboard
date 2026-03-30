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
import com.orchestradashboard.shared.ui.projectexplorer.ProjectExplorerViewModel

sealed class Screen {
    data object Dashboard : Screen()

    data class AgentDetail(val agentId: String) : Screen()

    data object ProjectExplorer : Screen()
}

@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    agentDetailViewModelFactory: (String) -> AgentDetailViewModel,
    projectExplorerViewModelFactory: () -> ProjectExplorerViewModel,
    modifier: Modifier = Modifier,
) {
    var currentScreen: Screen by remember { mutableStateOf(Screen.Dashboard) }

    when (val screen = currentScreen) {
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
                onBackClick = { currentScreen = Screen.Dashboard },
                modifier = modifier,
            )
        }
        is Screen.ProjectExplorer -> {
            val vm = remember { projectExplorerViewModelFactory() }
            DisposableEffect(Unit) {
                onDispose { vm.onCleared() }
            }
            ProjectExplorerScreen(
                viewModel = vm,
                onBackClick = { currentScreen = Screen.Dashboard },
                modifier = modifier,
            )
        }
    }
}
