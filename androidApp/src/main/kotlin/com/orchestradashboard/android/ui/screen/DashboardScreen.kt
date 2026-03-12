package com.orchestradashboard.android.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.orchestradashboard.shared.domain.model.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
    com.orchestradashboard.shared.ui.screen.DashboardScreen(viewModel, modifier)
}
