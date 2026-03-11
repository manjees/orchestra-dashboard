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

private fun formatRelativeTime(epochMs: Long): String {
    val now = System.currentTimeMillis()
    val diffSeconds = (now - epochMs) / 1000
    return when {
        diffSeconds < 0 -> "just now"
        diffSeconds < 60 -> "${diffSeconds}s ago"
        diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
        diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
        else -> "${diffSeconds / 86400}d ago"
    }
}
