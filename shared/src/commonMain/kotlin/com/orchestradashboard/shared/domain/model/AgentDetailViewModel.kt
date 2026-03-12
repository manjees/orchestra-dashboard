package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.repository.EventRepository
import com.orchestradashboard.shared.domain.repository.PipelineRepository
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
    private val agentRepository: AgentRepository,
    private val pipelineRepository: PipelineRepository,
    private val eventRepository: EventRepository,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(AgentDetailUiState())
    val uiState: StateFlow<AgentDetailUiState> = _uiState.asStateFlow()

    private var isObserving = false

    fun startObserving() {
        if (isObserving) return
        isObserving = true
        observeAgent()
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

    private fun observeAgent() {
        viewModelScope.launch {
            agentRepository.observeAgent(agentId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Unknown error",
                            isLoading = false,
                        )
                    }
                }
                .collect { agent ->
                    val isFirstEmission = _uiState.value.agent == null
                    _uiState.update {
                        it.copy(agent = agent, isLoading = false)
                    }
                    if (isFirstEmission) {
                        observePipelines()
                        observeEvents()
                    }
                }
        }
    }

    private fun observePipelines() {
        viewModelScope.launch {
            pipelineRepository.observePipelineRuns(agentId)
                .catch { /* Pipeline errors don't clear agent data */ }
                .collect { runs ->
                    _uiState.update { it.copy(pipelineRuns = runs) }
                }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventRepository.observeEvents(agentId)
                .catch { /* Event errors don't clear agent data */ }
                .collect { events ->
                    _uiState.update { it.copy(events = events) }
                }
        }
    }
}
