package com.orchestradashboard.desktop

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.orchestradashboard.desktop.di.AppContainer
import com.orchestradashboard.shared.ui.navigation.AppNavigator
import com.orchestradashboard.shared.ui.navigation.NavigationState
import com.orchestradashboard.shared.ui.theme.DashboardTheme

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Orchestra Dashboard",
        ) {
            val dashboardViewModel = remember { AppContainer.createDashboardViewModel() }
            var navigationState by remember { mutableStateOf<NavigationState>(NavigationState.Dashboard) }

            DisposableEffect(dashboardViewModel) {
                onDispose { dashboardViewModel.onCleared() }
            }

            DashboardTheme {
                AppNavigator(
                    navigationState = navigationState,
                    dashboardViewModel = dashboardViewModel,
                    agentDetailViewModelFactory = { AppContainer.createAgentDetailViewModel(it) },
                    onNavigate = { navigationState = it },
                )
            }
        }
    }
