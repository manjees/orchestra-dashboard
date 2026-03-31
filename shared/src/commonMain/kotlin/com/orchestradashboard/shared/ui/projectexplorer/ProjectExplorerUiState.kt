package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Checkpoint

sealed class RetryResult {
    data class Success(val checkpointId: String) : RetryResult()

    data class Failure(val checkpointId: String, val message: String) : RetryResult()
}

data class ProjectExplorerUiState(
    val checkpoints: List<Checkpoint> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val retryingCheckpointId: String? = null,
    val retryResult: RetryResult? = null,
)
