package com.orchestradashboard.shared.ui.analytics

import com.orchestradashboard.shared.domain.model.PeriodFilter
import com.orchestradashboard.shared.domain.usecase.GetDurationTrendsUseCase
import com.orchestradashboard.shared.domain.usecase.GetPipelineAnalyticsUseCase
import com.orchestradashboard.shared.domain.usecase.GetStepFailureRatesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

class AnalyticsViewModel(
    private val getPipelineAnalyticsUseCase: GetPipelineAnalyticsUseCase,
    private val getDurationTrendsUseCase: GetDurationTrendsUseCase,
    private val getStepFailureRatesUseCase: GetStepFailureRatesUseCase,
    private val project: String,
    private val clock: Clock = Clock.System,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun loadData() {
        val period = _uiState.value.selectedPeriod
        val (from, granularity) = periodToParams(period)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val summaryDeferred = async { getPipelineAnalyticsUseCase(project, from) }
            val trendsDeferred = async { getDurationTrendsUseCase(project, granularity) }
            val failuresDeferred = async { getStepFailureRatesUseCase(project) }

            val summaryResult = summaryDeferred.await()
            val trendsResult = trendsDeferred.await()
            val failuresResult = failuresDeferred.await()

            _uiState.update { state ->
                state.copy(
                    summary = summaryResult.getOrNull(),
                    durationTrends = trendsResult.getOrDefault(emptyList()),
                    stepFailures = failuresResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error =
                        summaryResult.exceptionOrNull()?.message
                            ?: trendsResult.exceptionOrNull()?.message
                            ?: failuresResult.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun selectPeriod(period: PeriodFilter) {
        _uiState.update { it.copy(selectedPeriod = period) }
        reloadPeriodData()
    }

    fun refresh() {
        loadData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }

    // Only reloads period-dependent data (summary + trends). Step failures are period-independent
    // and must not be re-fetched on every filter change.
    private fun reloadPeriodData() {
        val period = _uiState.value.selectedPeriod
        val (from, granularity) = periodToParams(period)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val summaryDeferred = async { getPipelineAnalyticsUseCase(project, from) }
            val trendsDeferred = async { getDurationTrendsUseCase(project, granularity) }

            val summaryResult = summaryDeferred.await()
            val trendsResult = trendsDeferred.await()

            _uiState.update { state ->
                state.copy(
                    summary = summaryResult.getOrNull(),
                    durationTrends = trendsResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error =
                        summaryResult.exceptionOrNull()?.message
                            ?: trendsResult.exceptionOrNull()?.message,
                )
            }
        }
    }

    private fun periodToParams(period: PeriodFilter): Pair<Long?, String> =
        when (period) {
            PeriodFilter.WEEK -> clock.now().minus(7.days).toEpochMilliseconds() to "day"
            PeriodFilter.MONTH -> clock.now().minus(30.days).toEpochMilliseconds() to "day"
            PeriodFilter.ALL -> null to "week"
        }
}
