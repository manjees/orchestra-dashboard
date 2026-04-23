package com.orchestradashboard.shared.ui.logstream

import com.orchestradashboard.shared.domain.model.LogEntry
import com.orchestradashboard.shared.domain.model.LogStreamState

data class LogStreamUiState(
    val streamState: LogStreamState = LogStreamState.Idle,
    val logs: List<LogEntry> = emptyList(),
    val selectedStepId: String? = null,
)
