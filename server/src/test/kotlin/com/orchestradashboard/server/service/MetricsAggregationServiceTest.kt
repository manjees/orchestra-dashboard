package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.MetricEntity
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.repository.MetricJpaRepository
import com.orchestradashboard.server.repository.MetricsAggregateJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

class MetricsAggregationServiceTest {
    private val metricRepository: MetricJpaRepository = mock()
    private val aggregateRepository: MetricsAggregateJpaRepository = mock()
    private val agentRepository: AgentJpaRepository = mock()
    private val service = MetricsAggregationService(metricRepository, aggregateRepository, agentRepository)

    private val agentEntity = AgentEntity(id = "agent-1", name = "Test Agent", type = "WORKER", status = "RUNNING")

    @Test
    fun `should calculate average CPU usage over last hour correctly`() {
        val now = System.currentTimeMillis()
        val from = now - 3_600_000L
        val metrics =
            listOf(
                MetricEntity(id = "m1", agentId = "agent-1", name = "cpu_usage", value = 40.0, unit = "percent", timestamp = from + 100),
                MetricEntity(id = "m2", agentId = "agent-1", name = "cpu_usage", value = 60.0, unit = "percent", timestamp = from + 200),
                MetricEntity(id = "m3", agentId = "agent-1", name = "cpu_usage", value = 80.0, unit = "percent", timestamp = from + 300),
                MetricEntity(id = "m4", agentId = "agent-1", name = "cpu_usage", value = 50.0, unit = "percent", timestamp = from + 400),
                MetricEntity(id = "m5", agentId = "agent-1", name = "cpu_usage", value = 70.0, unit = "percent", timestamp = from + 500),
                MetricEntity(id = "m6", agentId = "agent-1", name = "cpu_usage", value = 100.0, unit = "percent", timestamp = from + 600),
            )

        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agentEntity))
        whenever(
            metricRepository.findByAgentIdAndNameAndTimestampBetween(
                eq("agent-1"),
                eq("cpu_usage"),
                any(),
                any(),
            ),
        ).thenReturn(metrics)

        val results = service.getAggregatedMetrics("agent-1", from, now, "cpu_usage")

        assertEquals(1, results.size)
        val result = results[0]
        assertEquals(6, result.dataPoints.size)
        assertEquals(66.67, result.average!!, 0.01)
        assertEquals(40.0, result.min)
        assertEquals(100.0, result.max)
    }

    @Test
    fun `should aggregate multiple metric types independently`() {
        val now = System.currentTimeMillis()
        val from = now - 3_600_000L
        val cpuMetrics =
            listOf(
                MetricEntity(id = "m1", agentId = "agent-1", name = "cpu_usage", value = 50.0, unit = "percent", timestamp = from + 100),
                MetricEntity(id = "m2", agentId = "agent-1", name = "cpu_usage", value = 70.0, unit = "percent", timestamp = from + 200),
            )

        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agentEntity))
        whenever(
            metricRepository.findByAgentIdAndNameAndTimestampBetween(
                eq("agent-1"),
                eq("cpu_usage"),
                any(),
                any(),
            ),
        ).thenReturn(cpuMetrics)

        val results = service.getAggregatedMetrics("agent-1", from, now, "cpu_usage")

        assertEquals(1, results.size)
        assertEquals("cpu_usage", results[0].metricName)
        assertEquals(2, results[0].dataPoints.size)
    }

    @Test
    fun `should only include metrics within the requested time range`() {
        val metrics =
            listOf(
                MetricEntity(id = "m2", agentId = "agent-1", name = "cpu_usage", value = 60.0, unit = "percent", timestamp = 200),
                MetricEntity(id = "m3", agentId = "agent-1", name = "cpu_usage", value = 80.0, unit = "percent", timestamp = 300),
            )

        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agentEntity))
        whenever(
            metricRepository.findByAgentIdAndNameAndTimestampBetween(
                eq("agent-1"),
                eq("cpu_usage"),
                eq(150L),
                eq(350L),
            ),
        ).thenReturn(metrics)

        val results = service.getAggregatedMetrics("agent-1", 150L, 350L, "cpu_usage")

        assertEquals(1, results.size)
        assertEquals(2, results[0].dataPoints.size)
        assertEquals(200L, results[0].dataPoints[0].timestamp)
        assertEquals(300L, results[0].dataPoints[1].timestamp)
    }

    @Test
    fun `should return empty data when no metrics exist in range`() {
        val now = System.currentTimeMillis()
        val from = now - 3_600_000L

        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agentEntity))
        whenever(
            metricRepository.findByAgentIdAndNameAndTimestampBetween(
                eq("agent-1"),
                eq("cpu_usage"),
                any(),
                any(),
            ),
        ).thenReturn(emptyList())

        val results = service.getAggregatedMetrics("agent-1", from, now, "cpu_usage")

        assertEquals(1, results.size)
        val result = results[0]
        assertTrue(result.dataPoints.isEmpty())
        assertNull(result.average)
        assertNull(result.min)
        assertNull(result.max)
        assertEquals(0, result.sampleCount)
    }

    @Test
    fun `should throw NoSuchElementException when agent does not exist`() {
        whenever(agentRepository.findById("nonexistent")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.getAggregatedMetrics("nonexistent", null, null, null)
        }
    }

    @Test
    fun `should throw IllegalArgumentException when from is after to`() {
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agentEntity))

        assertThrows<IllegalArgumentException> {
            service.getAggregatedMetrics("agent-1", 500L, 100L, null)
        }
    }

    @Test
    fun `should return all metric types when metricName is null`() {
        val now = System.currentTimeMillis()
        val from = now - 3_600_000L
        val allMetrics =
            listOf(
                MetricEntity(id = "m1", agentId = "agent-1", name = "cpu_usage", value = 50.0, unit = "percent", timestamp = from + 100),
                MetricEntity(id = "m2", agentId = "agent-1", name = "memory_usage", value = 70.0, unit = "percent", timestamp = from + 200),
            )

        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agentEntity))
        whenever(
            metricRepository.findByAgentIdAndTimestampBetween(eq("agent-1"), any(), any()),
        ).thenReturn(allMetrics)

        val results = service.getAggregatedMetrics("agent-1", from, now, null)

        assertEquals(2, results.size)
        val names = results.map { it.metricName }.toSet()
        assertTrue(names.contains("cpu_usage"))
        assertTrue(names.contains("memory_usage"))
    }
}
