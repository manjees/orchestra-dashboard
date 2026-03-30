package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.GetAggregatedMetricsUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
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

class DashboardViewModel(
    private val observeAgentsUseCase: ObserveAgentsUseCase,
    private val getAgentUseCase: GetAgentUseCase,
    private val getAggregatedMetricsUseCase: GetAggregatedMetricsUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(DashboardUiState())
    private var paginatedJob: Job? = null

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

    fun loadPage(page: Int) {
        paginatedJob?.cancel()
        paginatedJob =
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val currentPageSize = _uiState.value.pageSize
                observeAgentsUseCase(page, currentPageSize)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                error = e.message ?: "Unknown error",
                                isLoading = false,
                            )
                        }
                    }
                    .collect { pagedResult ->
                        _uiState.update {
                            it.copy(
                                agents = pagedResult.agents,
                                isLoading = false,
                                currentPage = pagedResult.page,
                                totalElements = pagedResult.totalElements,
                                totalPages = pagedResult.totalPages,
                                connectionStatus = ConnectionStatus.CONNECTED,
                            )
                        }
                    }
            }
    }

    fun nextPage() {
        val state = _uiState.value
        if (state.hasNextPage) loadPage(state.currentPage + 1)
    }

    fun previousPage() {
        val state = _uiState.value
        if (state.hasPreviousPage) loadPage(state.currentPage - 1)
    }

    fun selectAgent(agentId: String?) {
        if (agentId == null) {
            _uiState.update { it.copy(selectedAgent = null) }
            return
        }
        viewModelScope.launch {
            getAgentUseCase(agentId).fold(
                onSuccess = { agent ->
                    _uiState.update { it.copy(selectedAgent = agent) }
                    loadMetrics(agentId)
                },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Unknown error") } },
            )
        }
    }

    fun loadMetrics(agentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(metricsChart = it.metricsChart.copy(isLoading = true, error = null)) }
            val timeRange = _uiState.value.metricsChart.selectedTimeRange
            getAggregatedMetricsUseCase(agentId, timeRange).fold(
                onSuccess = { metrics ->
                    val byName = metrics.groupBy { it.name }
                    val names = byName.keys.toList().sorted()
                    val selected =
                        _uiState.value.metricsChart.selectedMetricName
                            ?.takeIf { it in names } ?: names.firstOrNull()
                    val points =
                        selected?.let { name ->
                            byName[name]
                                ?.sortedBy { it.timestamp }
                                ?.map { TimeSeriesPoint(it.timestamp, it.value) }
                        } ?: emptyList()

                    _uiState.update {
                        it.copy(
                            metricsChart =
                                MetricsChartState(
                                    points = points,
                                    selectedTimeRange = timeRange,
                                    availableMetricNames = names,
                                    selectedMetricName = selected,
                                    isLoading = false,
                                ),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            metricsChart =
                                it.metricsChart.copy(
                                    isLoading = false,
                                    error = e.message ?: "Failed to load metrics",
                                ),
                        )
                    }
                },
            )
        }
    }

    fun selectTimeRange(timeRange: TimeRange) {
        _uiState.update { it.copy(metricsChart = it.metricsChart.copy(selectedTimeRange = timeRange)) }
        _uiState.value.selectedAgent?.id?.let { loadMetrics(it) }
    }

    fun selectMetricName(name: String) {
        _uiState.update { it.copy(metricsChart = it.metricsChart.copy(selectedMetricName = name)) }
        _uiState.value.selectedAgent?.id?.let { loadMetrics(it) }
    }

    fun setStatusFilter(status: Agent.AgentStatus?) {
        _uiState.update { current ->
            val newFilter = if (current.statusFilter == status) null else status
            current.copy(statusFilter = newFilter)
        }
    }

    fun clearMetricsError() {
        _uiState.update { it.copy(metricsChart = it.metricsChart.copy(error = null)) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /** Must be called when the screen is destroyed to cancel background coroutines */
    fun onCleared() {
        viewModelScope.cancel()
    }
}
