package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class IssueMapperTest {
    private val mapper = IssueMapper()

    @Test
    fun `maps OrchestratorIssueDto to Issue with Instant createdAt`() {
        val dto =
            OrchestratorIssueDto(
                number = 42,
                title = "Fix login bug",
                labels = listOf("bug", "priority:high"),
                state = "open",
                createdAt = "2025-01-15T10:30:00Z",
            )
        val result = mapper.toDomain(dto)
        assertEquals(42, result.number)
        assertEquals("Fix login bug", result.title)
        assertEquals(listOf("bug", "priority:high"), result.labels)
        assertEquals("open", result.state)
        assertEquals(Instant.parse("2025-01-15T10:30:00Z"), result.createdAt)
    }

    @Test
    fun `maps list of DTOs preserving order`() {
        val dtos =
            listOf(
                OrchestratorIssueDto(1, "First", emptyList(), "open", "2025-01-01T00:00:00Z"),
                OrchestratorIssueDto(2, "Second", emptyList(), "closed", "2025-01-02T00:00:00Z"),
            )
        val result = mapper.toDomain(dtos)
        assertEquals(2, result.size)
        assertEquals(1, result[0].number)
        assertEquals(2, result[1].number)
    }

    @Test
    fun `handles empty labels list`() {
        val dto =
            OrchestratorIssueDto(
                number = 1,
                title = "No labels",
                labels = emptyList(),
                state = "open",
                createdAt = "2025-06-01T12:00:00Z",
            )
        val result = mapper.toDomain(dto)
        assertEquals(emptyList(), result.labels)
    }

    @Test
    fun `parses ISO-8601 createdAt string to Instant`() {
        val dto =
            OrchestratorIssueDto(
                number = 10,
                title = "Test",
                labels = emptyList(),
                state = "open",
                createdAt = "2024-12-25T18:45:30Z",
            )
        val result = mapper.toDomain(dto)
        assertEquals(Instant.parse("2024-12-25T18:45:30Z"), result.createdAt)
    }
}
