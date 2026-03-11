package com.orchestradashboard.android

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.orchestradashboard.android.di.AppContainer
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.ui.navigation.AppNavigator
import com.orchestradashboard.shared.ui.navigation.NavigationState
import com.orchestradashboard.shared.ui.theme.DashboardTheme

class OrchestraApplication : Application()

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: DashboardViewModel
    private var navigationState by mutableStateOf<NavigationState>(NavigationState.Dashboard)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = AppContainer.createDashboardViewModel()

        setContent {
            DashboardTheme {
                AppNavigator(
                    navigationState = navigationState,
                    dashboardViewModel = viewModel,
                    agentDetailViewModelFactory = { AppContainer.createAgentDetailViewModel(it) },
                    onNavigate = { navigationState = it },
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onCleared()
    }
}
