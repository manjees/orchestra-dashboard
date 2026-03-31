package com.orchestradashboard.shared.ui.dashboardhome

import com.orchestradashboard.shared.data.mapper.SystemStatusMapper
import com.orchestradashboard.shared.domain.model.ConnectionStatus
import com.orchestradashboard.shared.domain.repository.SystemEventData
import com.orchestradashboard.shared.domain.usecase.GetActivePipelinesUseCase
import com.orchestradashboard.shared.domain.usecase.GetPipelineHistoryUseCase
import com.orchestradashboard.shared.domain.usecase.GetSystemStatusUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveSystemEventsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardHomeViewModel(
    private val getSystemStatusUseCase: GetSystemStatusUseCase,
    private val getActivePipelinesUseCase: GetActivePipelinesUseCase,
    private val getPipelineHistoryUseCase: GetPipelineHistoryUseCase,
    private val observeSystemEventsUseCase: ObserveSystemEventsUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(DashboardHomeUiState())
    val uiState: StateFlow<DashboardHomeUiState> = _uiState.asStateFlow()

    private val thermalParser = SystemStatusMapper()

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val statusDeferred = async { getSystemStatusUseCase() }
            val pipelinesDeferred = async { getActivePipelinesUseCase() }
            val historyDeferred = async { getPipelineHistoryUseCase() }

            val statusResult = statusDeferred.await()
            val pipelinesResult = pipelinesDeferred.await()
            val historyResult = historyDeferred.await()

            _uiState.update { state ->
                state.copy(
                    systemStatus = statusResult.getOrNull(),
                    activePipelines = pipelinesResult.getOrDefault(emptyList()),
                    recentResults = historyResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error =
                        statusResult.exceptionOrNull()?.message
                            ?: pipelinesResult.exceptionOrNull()?.message
                            ?: historyResult.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun startObserving() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTED) }
            observeSystemEventsUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            connectionStatus = ConnectionStatus.DISCONNECTED,
                            error = e.message,
                        )
                    }
                }
                .collect { event -> handleEvent(event) }
        }
    }

    private fun handleEvent(event: SystemEventData) {
        if (event.ramPercent != null || event.cpuPercent != null || event.thermal != null) {
            _uiState.update { state ->
                val current = state.systemStatus ?: return@update state
                state.copy(
                    systemStatus =
                        current.copy(
                            ramPercent = event.ramPercent ?: current.ramPercent,
                            cpuPercent = event.cpuPercent ?: current.cpuPercent,
                            thermalPressure =
                                event.thermal
                                    ?.let { thermalParser.parseThermalPressure(it) }
                                    ?: current.thermalPressure,
                        ),
                )
            }
        }
        if (event.step != null || event.status != null) {
            viewModelScope.launch {
                getActivePipelinesUseCase().onSuccess { pipelines ->
                    _uiState.update { it.copy(activePipelines = pipelines) }
                }
            }
        }
    }

    fun refresh() {
        loadInitialData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
