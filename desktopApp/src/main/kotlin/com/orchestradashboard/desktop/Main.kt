package com.orchestradashboard.desktop

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.orchestradashboard.desktop.di.AppContainer
import com.orchestradashboard.desktop.ui.screen.DashboardScreen
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
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
