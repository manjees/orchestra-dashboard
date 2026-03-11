package com.orchestradashboard.android

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.orchestradashboard.android.di.AppContainer
import com.orchestradashboard.android.ui.screen.DashboardScreen
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.ui.theme.DashboardTheme

class OrchestraApplication : Application()

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = AppContainer.createDashboardViewModel()

        setContent {
            DashboardTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onCleared()
    }
}
