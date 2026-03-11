package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.repository.PipelineRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveEventsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val pipelineRepository: PipelineRepository,
    private val observeEventsUseCase: ObserveEventsUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(AgentDetailUiState())
    val uiState: StateFlow<AgentDetailUiState> = _uiState.asStateFlow()

    fun loadAgent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getAgentUseCase(agentId).fold(
                onSuccess = { agent -> _uiState.update { it.copy(agent = agent, isLoading = false) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) } },
            )
        }
    }

    fun startObserving() {
        loadAgent()
        viewModelScope.launch {
            pipelineRepository.observePipelineRuns(agentId)
                .catch { e -> _uiState.update { it.copy(error = e.message ?: "Pipeline error") } }
                .collect { runs -> _uiState.update { it.copy(pipelineRuns = runs) } }
        }
        viewModelScope.launch {
            observeEventsUseCase(agentId)
                .catch { e -> _uiState.update { it.copy(error = e.message ?: "Event error") } }
                .collect { events -> _uiState.update { it.copy(events = events) } }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index.coerceIn(0, 2)) }
    }

    fun togglePipelineExpanded(pipelineId: String) {
        _uiState.update { state ->
            val newSet = state.expandedPipelineIds.toMutableSet()
            if (!newSet.add(pipelineId)) newSet.remove(pipelineId)
            state.copy(expandedPipelineIds = newSet)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
