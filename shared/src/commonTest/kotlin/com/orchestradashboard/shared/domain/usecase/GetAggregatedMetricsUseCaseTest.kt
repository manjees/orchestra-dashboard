package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Metric
import com.orchestradashboard.shared.domain.model.TimeRange
import com.orchestradashboard.shared.domain.repository.MetricRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeMetricRepository(
    private val aggregatedMetrics: List<Metric> = emptyList(),
    private val shouldFail: Boolean = false,
) : MetricRepository {
    override fun observeMetrics(agentId: String): Flow<List<Metric>> = flowOf(emptyList())

    override suspend fun getFleetMetrics(): Result<List<Metric>> = Result.success(emptyList())

    override suspend fun getAggregatedMetrics(
        agentId: String,
        timeRange: TimeRange,
    ): Result<List<Metric>> =
        if (shouldFail) {
            Result.failure(RuntimeException("API error"))
        } else {
            Result.success(aggregatedMetrics)
        }
}

class GetAggregatedMetricsUseCaseTest {
    @Test
    fun `should return metrics from repository`() =
        runTest {
            val expected =
                listOf(
                    Metric(agentId = "agent-1", name = "cpu_usage", value = 42.0, unit = "percent", timestamp = 1000L),
                )
            val fakeRepo = FakeMetricRepository(aggregatedMetrics = expected)
            val useCase = GetAggregatedMetricsUseCase(fakeRepo)

            val result = useCase("agent-1", TimeRange.Last24Hours)

            assertTrue(result.isSuccess)
            assertEquals(expected, result.getOrNull())
        }

    @Test
    fun `should propagate failure from repository`() =
        runTest {
            val fakeRepo = FakeMetricRepository(shouldFail = true)
            val useCase = GetAggregatedMetricsUseCase(fakeRepo)

            val result = useCase("agent-1", TimeRange.Last24Hours)

            assertTrue(result.isFailure)
        }
}
