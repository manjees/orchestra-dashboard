package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
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

class DashboardViewModel(
    private val observeAgentsUseCase: ObserveAgentsUseCase,
    private val getAgentUseCase: GetAgentUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(DashboardUiState())

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun startObserving() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            observeAgentsUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Unknown error",
                            isLoading = false,
                            connectionStatus = ConnectionStatus.DISCONNECTED,
                        )
                    }
                }
                .collect { agents ->
                    _uiState.update {
                        it.copy(
                            agents = agents,
                            isLoading = false,
                            connectionStatus = ConnectionStatus.CONNECTED,
                        )
                    }
                }
        }
    }

    fun selectAgent(agentId: String?) {
        if (agentId == null) {
            _uiState.update { it.copy(selectedAgent = null) }
            return
        }
        viewModelScope.launch {
            getAgentUseCase(agentId).fold(
                onSuccess = { agent -> _uiState.update { it.copy(selectedAgent = agent) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Unknown error") } },
            )
        }
    }

    fun setStatusFilter(status: Agent.AgentStatus?) {
        _uiState.update { current ->
            val newFilter = if (current.statusFilter == status) null else status
            current.copy(statusFilter = newFilter)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /** Must be called when the screen is destroyed to cancel background coroutines */
    fun onCleared() {
        viewModelScope.cancel()
    }
}
