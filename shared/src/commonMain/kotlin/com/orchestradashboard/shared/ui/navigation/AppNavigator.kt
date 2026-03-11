package com.orchestradashboard.shared.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.orchestradashboard.shared.domain.model.AgentDetailViewModel
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.ui.screen.AgentDetailScreen
import com.orchestradashboard.shared.ui.screen.DashboardScreen

@Composable
fun AppNavigator(
    navigationState: NavigationState,
    dashboardViewModel: DashboardViewModel,
    agentDetailViewModelFactory: (String) -> AgentDetailViewModel,
    onNavigate: (NavigationState) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (navigationState) {
        is NavigationState.Dashboard -> {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onAgentClick = { agentId -> onNavigate(NavigationState.AgentDetail(agentId)) },
                modifier = modifier,
            )
        }
        is NavigationState.AgentDetail -> {
            val detailViewModel =
                remember(navigationState.agentId) {
                    agentDetailViewModelFactory(navigationState.agentId)
                }
            DisposableEffect(detailViewModel) {
                onDispose { detailViewModel.onCleared() }
            }
            AgentDetailScreen(
                viewModel = detailViewModel,
                onBack = { onNavigate(NavigationState.Dashboard) },
                modifier = modifier,
            )
        }
    }
}
