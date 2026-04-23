package com.orchestradashboard.shared.ui.logstream

import com.orchestradashboard.shared.domain.model.LogStreamState
import com.orchestradashboard.shared.domain.usecase.ObserveLogStreamUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogStreamViewModel(
    private val observeLogStreamUseCase: ObserveLogStreamUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(LogStreamUiState())
    val uiState: StateFlow<LogStreamUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null

    fun startStream(stepId: String) {
        val current = _uiState.value
        if (current.selectedStepId == stepId && current.streamState is LogStreamState.Streaming) {
            return
        }

        streamJob?.cancel()
        _uiState.update {
            it.copy(
                streamState = LogStreamState.Loading,
                logs = emptyList(),
                selectedStepId = stepId,
            )
        }

        streamJob =
            viewModelScope.launch {
                observeLogStreamUseCase(stepId)
                    .onStart {
                        _uiState.update { it.copy(streamState = LogStreamState.Streaming) }
                    }
                    .catch { e ->
                        _uiState.update {
                            it.copy(streamState = LogStreamState.Error(e.message ?: "Unknown error"))
                        }
                    }
                    .collect { entry ->
                        _uiState.update { state ->
                            state.copy(
                                logs =
                                    buildList {
                                        addAll(state.logs)
                                        add(entry)
                                    },
                            )
                        }
                    }
            }
    }

    fun stopStream() {
        streamJob?.cancel()
        streamJob = null
        _uiState.update {
            it.copy(streamState = LogStreamState.Idle, selectedStepId = null, logs = emptyList())
        }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
