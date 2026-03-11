package com.orchestradashboard.shared.ui.agentdetail

import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveEventsUseCase
import com.orchestradashboard.shared.domain.usecase.ObservePipelineRunsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AgentDetailViewModel(
    private val agentId: String,
    private val getAgentUseCase: GetAgentUseCase,
    private val observePipelineRunsUseCase: ObservePipelineRunsUseCase,
    private val observeEventsUseCase: ObserveEventsUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(AgentDetailUiState())
    val uiState: StateFlow<AgentDetailUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var dataStreamsJob: Job? = null

    fun loadAgent() {
        if (agentId.isBlank()) {
            _uiState.update { it.copy(error = "Invalid agent ID") }
            return
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getAgentUseCase(agentId).fold(
                onSuccess = { agent ->
                    _uiState.update { it.copy(agent = agent, isLoading = false) }
                    startObservingDataStreams()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
                },
            )
        }
    }

    private fun startObservingDataStreams() {
        dataStreamsJob?.cancel()
        dataStreamsJob = viewModelScope.launch {
            launch {
                observePipelineRunsUseCase(agentId)
                    .catch { e -> _uiState.update { it.copy(error = e.message ?: "Unknown pipeline error") } }
                    .collect { runs -> _uiState.update { it.copy(pipelineRuns = runs) } }
            }
            launch {
                observeEventsUseCase(agentId)
                    .catch { e -> _uiState.update { it.copy(error = e.message ?: "Unknown event error") } }
                    .collect { events -> _uiState.update { it.copy(events = events) } }
            }
        }
    }

    fun selectTab(tab: DetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
