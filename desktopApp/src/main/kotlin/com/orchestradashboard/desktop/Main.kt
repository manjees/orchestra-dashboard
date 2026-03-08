package com.orchestradashboard.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.orchestradashboard.desktop.di.AppContainer
import com.orchestradashboard.desktop.ui.screen.DashboardScreen

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

            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
