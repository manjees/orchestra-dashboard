package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PipelineHistoryMapperTest {
    private val mapper = PipelineHistoryMapper()

    private fun createHistoryDto(
        id: String = "hist-1",
        projectName: String = "my-project",
        issueNum: Int = 42,
        status: String = "PASSED",
        elapsedTotalSec: Double = 300.0,
        completedAt: String? = "2024-01-01T01:00:00Z",
    ) = PipelineHistoryDto(
        id = id,
        projectName = projectName,
        issueNum = issueNum,
        status = status,
        startedAt = "2024-01-01T00:00:00Z",
        completedAt = completedAt,
        elapsedTotalSec = elapsedTotalSec,
    )

    @Test
    fun `maps PipelineHistoryDto to PipelineResult with all fields`() {
        val dto = createHistoryDto()
        val result = mapper.toDomain(dto)

        assertEquals("hist-1", result.id)
        assertEquals("my-project", result.projectName)
        assertEquals(42, result.issueNum)
        assertEquals(PipelineRunStatus.PASSED, result.status)
        assertEquals(300.0, result.elapsedTotalSec)
        assertEquals("2024-01-01T01:00:00Z", result.completedAt)
    }

    @Test
    fun `maps status string PASSED to PipelineRunStatus PASSED`() {
        val result = mapper.toDomain(createHistoryDto(status = "PASSED"))
        assertEquals(PipelineRunStatus.PASSED, result.status)
    }

    @Test
    fun `maps status string FAILED to PipelineRunStatus FAILED`() {
        val result = mapper.toDomain(createHistoryDto(status = "FAILED"))
        assertEquals(PipelineRunStatus.FAILED, result.status)
    }

    @Test
    fun `maps unknown status to PipelineRunStatus FAILED as default`() {
        val result = mapper.toDomain(createHistoryDto(status = "UNKNOWN_STATUS"))
        assertEquals(PipelineRunStatus.FAILED, result.status)
    }

    @Test
    fun `maps null completedAt to null`() {
        val result = mapper.toDomain(createHistoryDto(completedAt = null))
        assertNull(result.completedAt)
    }

    @Test
    fun `maps list of history DTOs to domain models`() {
        val dtos =
            listOf(
                createHistoryDto(id = "h1"),
                createHistoryDto(id = "h2"),
                createHistoryDto(id = "h3"),
            )
        val results = mapper.toDomainList(dtos)

        assertEquals(3, results.size)
        assertEquals("h1", results[0].id)
        assertEquals("h3", results[2].id)
    }
}
