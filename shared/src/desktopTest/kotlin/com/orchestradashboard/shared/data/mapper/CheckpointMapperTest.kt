package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckpointMapperTest {
    private val mapper = CheckpointMapper()

    @Test
    fun `maps CheckpointDto to Checkpoint with Instant createdAt`() {
        val dto =
            CheckpointDto(
                id = "cp-001",
                pipelineId = "pipe-42",
                createdAt = "2025-03-10T08:00:00Z",
                step = "lint",
                status = "passed",
            )
        val result = mapper.toDomain(dto)
        assertEquals("cp-001", result.id)
        assertEquals("pipe-42", result.pipelineId)
        assertEquals(Instant.parse("2025-03-10T08:00:00Z"), result.createdAt)
        assertEquals("lint", result.step)
        assertEquals(CheckpointStatus.PASSED, result.status)
    }

    @Test
    fun `maps checkpoint status string to CheckpointStatus enum`() {
        val statuses =
            mapOf(
                "passed" to CheckpointStatus.PASSED,
                "failed" to CheckpointStatus.FAILED,
                "pending" to CheckpointStatus.PENDING,
                "PASSED" to CheckpointStatus.PASSED,
                "FAILED" to CheckpointStatus.FAILED,
                "Pending" to CheckpointStatus.PENDING,
            )
        statuses.forEach { (input, expected) ->
            val dto = CheckpointDto("id", "pipe", "2025-01-01T00:00:00Z", "step", input)
            assertEquals(expected, mapper.toDomain(dto).status, "Failed for input: $input")
        }
    }

    @Test
    fun `unknown status maps to UNKNOWN`() {
        val dto =
            CheckpointDto(
                id = "cp-002",
                pipelineId = "pipe-99",
                createdAt = "2025-06-15T12:00:00Z",
                step = "deploy",
                status = "cancelled",
            )
        assertEquals(CheckpointStatus.UNKNOWN, mapper.toDomain(dto).status)
    }
}
