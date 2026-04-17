package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.PipelineHistoryEntity
import com.orchestradashboard.server.model.PipelineStepHistoryEntity
import com.orchestradashboard.server.repository.PipelineHistoryJpaRepository
import com.orchestradashboard.server.repository.PipelineStepHistoryJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PipelineAnalyticsServiceTest {
    private val historyRepository: PipelineHistoryJpaRepository = mock()
    private val stepRepository: PipelineStepHistoryJpaRepository = mock()
    private val service = PipelineAnalyticsService(historyRepository, stepRepository)

    private fun makeHistory(
        id: String,
        status: String,
        elapsedSec: Double,
        startedAt: Long = 1700000000L,
    ) = PipelineHistoryEntity(
        id = id,
        projectName = "my-project",
        issueNum = 1,
        issueTitle = "Test",
        mode = "solve",
        status = status,
        startedAt = startedAt,
        elapsedTotalSec = elapsedSec,
    )

    @Test
    fun `getSummary calculates correct stats`() {
        val records =
            listOf(
                makeHistory("h1", "PASSED", 100.0),
                makeHistory("h2", "PASSED", 200.0),
                makeHistory("h3", "FAILED", 50.0),
                makeHistory("h4", "RUNNING", 0.0),
            )
        whenever(historyRepository.findByProjectNameAndStartedAtBetween(any(), any(), any()))
            .thenReturn(records)

        val result = service.getSummary("my-project", null, null)

        assertEquals("my-project", result.project)
        assertEquals(3, result.totalRuns) // PASSED + FAILED (RUNNING excluded)
        assertEquals(1, result.failedRuns)
        assertTrue(result.successRate > 0.66)
        assertTrue(result.avgDurationSec > 0)
    }

    @Test
    fun `getSummary with empty records returns zeros`() {
        whenever(historyRepository.findByProjectNameAndStartedAtBetween(any(), any(), any()))
            .thenReturn(emptyList())

        val result = service.getSummary("my-project", null, null)

        assertEquals(0, result.totalRuns)
        assertEquals(0, result.failedRuns)
        assertEquals(0.0, result.successRate)
        assertEquals(0.0, result.avgDurationSec)
    }

    @Test
    fun `getSummary with custom time range`() {
        whenever(historyRepository.findByProjectNameAndStartedAtBetween("my-project", 1000L, 2000L))
            .thenReturn(listOf(makeHistory("h1", "PASSED", 100.0)))

        val result = service.getSummary("my-project", 1000L, 2000L)

        assertEquals(1, result.totalRuns)
    }

    @Test
    fun `getStepFailureRates calculates rates correctly`() {
        val records =
            listOf(
                makeHistory("h1", "PASSED", 100.0),
                makeHistory("h2", "FAILED", 50.0),
            )
        whenever(historyRepository.findByProjectNameAndStartedAtBetween(any(), any(), any()))
            .thenReturn(records)

        val steps =
            listOf(
                PipelineStepHistoryEntity(id = "s1", pipelineHistoryId = "h1", stepName = "build", status = "PASSED"),
                PipelineStepHistoryEntity(id = "s2", pipelineHistoryId = "h2", stepName = "build", status = "FAILED"),
                PipelineStepHistoryEntity(id = "s3", pipelineHistoryId = "h1", stepName = "test", status = "PASSED"),
            )
        whenever(stepRepository.findByPipelineHistoryIdIn(listOf("h1", "h2"))).thenReturn(steps)

        val result = service.getStepFailureRates("my-project")

        assertEquals(2, result.size)
        val buildStep = result.find { it.stepName == "build" }!!
        assertEquals(2, buildStep.totalCount)
        assertEquals(1, buildStep.failedCount)
        assertEquals(0.5, buildStep.failureRate)

        val testStep = result.find { it.stepName == "test" }!!
        assertEquals(1, testStep.totalCount)
        assertEquals(0, testStep.failedCount)
    }

    @Test
    fun `getStepFailureRates returns empty when no records`() {
        whenever(historyRepository.findByProjectNameAndStartedAtBetween(any(), any(), any()))
            .thenReturn(emptyList())

        val result = service.getStepFailureRates("my-project")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getDurationTrends groups by day`() {
        val records =
            listOf(
                makeHistory("h1", "PASSED", 100.0, startedAt = 1700000000000L),
                makeHistory("h2", "PASSED", 200.0, startedAt = 1700000000000L),
                makeHistory("h3", "FAILED", 150.0, startedAt = 1700086400000L),
            )
        whenever(historyRepository.findByProjectNameAndStartedAtBetween(any(), any(), any()))
            .thenReturn(records)

        val result = service.getDurationTrends("my-project", "day")

        assertEquals(2, result.size)
        assertTrue(result[0].date < result[1].date)
        assertEquals(2, result[0].runCount)
        assertEquals(1, result[1].runCount)
    }

    @Test
    fun `getDurationTrends returns empty when no completed runs`() {
        val records = listOf(makeHistory("h1", "RUNNING", 0.0))
        whenever(historyRepository.findByProjectNameAndStartedAtBetween(any(), any(), any()))
            .thenReturn(records)

        val result = service.getDurationTrends("my-project", "day")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getDurationTrends groups by week`() {
        val records =
            listOf(
                makeHistory("h1", "PASSED", 100.0, startedAt = 1700000000000L),
                makeHistory("h2", "PASSED", 200.0, startedAt = 1700086400000L),
            )
        whenever(historyRepository.findByProjectNameAndStartedAtBetween(any(), any(), any()))
            .thenReturn(records)

        val result = service.getDurationTrends("my-project", "week")

        assertTrue(result.isNotEmpty())
    }
}
