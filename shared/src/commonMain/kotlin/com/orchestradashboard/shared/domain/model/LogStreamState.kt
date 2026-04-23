package com.orchestradashboard.shared.domain.model

sealed interface LogStreamState {
    data object Idle : LogStreamState

    data object Loading : LogStreamState

    data object Streaming : LogStreamState

    data class Error(val message: String) : LogStreamState
}
