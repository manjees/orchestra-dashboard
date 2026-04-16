package com.orchestradashboard.shared.ui.solvedialog

import com.orchestradashboard.shared.domain.model.SolveMode
import com.orchestradashboard.shared.domain.model.SolveResponse

data class SolveDialogState(
    val showDialog: Boolean = false,
    val projectName: String? = null,
    val selectedIssues: Set<Int> = emptySet(),
    val mode: SolveMode = SolveMode.AUTO,
    val isParallel: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: SolveResponse? = null,
)
