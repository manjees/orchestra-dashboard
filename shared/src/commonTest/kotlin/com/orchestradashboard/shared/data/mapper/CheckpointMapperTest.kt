package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckpointMapperTest {
    private val mapper = CheckpointMapper()

    @Test
    fun `toDomain maps DTO with failed status to FAILED`() {
        val dto =
            CheckpointDto(
                id = "cp-1",
                pipelineId = "pipe-1",
                createdAt = "2025-01-15T10:30:00Z",
                step = "build",
                status = "failed",
            )

        val result = mapper.toDomain(dto)

        assertEquals("cp-1", result.id)
        assertEquals("pipe-1", result.pipelineId)
        assertEquals("build", result.step)
        assertEquals(CheckpointStatus.FAILED, result.status)
    }

    @Test
    fun `toDomain maps DTO with passed status to PASSED`() {
        val dto =
            CheckpointDto(
                id = "cp-2",
                pipelineId = "pipe-2",
                createdAt = "2025-01-15T10:30:00Z",
                step = "test",
                status = "passed",
            )

        val result = mapper.toDomain(dto)

        assertEquals(CheckpointStatus.PASSED, result.status)
    }

    @Test
    fun `toDomain maps DTO with running status to RUNNING`() {
        val dto =
            CheckpointDto(
                id = "cp-3",
                pipelineId = "pipe-3",
                createdAt = "2025-01-15T10:30:00Z",
                step = "deploy",
                status = "running",
            )

        val result = mapper.toDomain(dto)

        assertEquals(CheckpointStatus.RUNNING, result.status)
    }

    @Test
    fun `toDomain maps unknown status string to UNKNOWN`() {
        val dto =
            CheckpointDto(
                id = "cp-4",
                pipelineId = "pipe-4",
                createdAt = "2025-01-15T10:30:00Z",
                step = "validate",
                status = "some_new_status",
            )

        val result = mapper.toDomain(dto)

        assertEquals(CheckpointStatus.UNKNOWN, result.status)
    }

    @Test
    fun `toDomain parses ISO timestamp to epoch millis`() {
        val dto =
            CheckpointDto(
                id = "cp-5",
                pipelineId = "pipe-5",
                createdAt = "2025-01-15T10:30:00Z",
                step = "build",
                status = "passed",
            )

        val result = mapper.toDomain(dto)

        assertEquals(1736937000000L, result.createdAt)
    }

    @Test
    fun `toDomain list variant maps all elements preserving order`() {
        val dtos =
            listOf(
                CheckpointDto("cp-a", "pipe-1", "2025-01-15T10:30:00Z", "build", "passed"),
                CheckpointDto("cp-b", "pipe-2", "2025-01-15T11:00:00Z", "test", "failed"),
                CheckpointDto("cp-c", "pipe-3", "2025-01-15T11:30:00Z", "deploy", "running"),
            )

        val results = mapper.toDomain(dtos)

        assertEquals(3, results.size)
        assertEquals("cp-a", results[0].id)
        assertEquals("cp-b", results[1].id)
        assertEquals("cp-c", results[2].id)
    }

    @Test
    fun `toDomain list variant maps empty list`() {
        val results = mapper.toDomain(emptyList())

        assertTrue(results.isEmpty())
    }

    @Test
    fun `toDomain handles case-insensitive status parsing`() {
        val dto =
            CheckpointDto(
                id = "cp-6",
                pipelineId = "pipe-6",
                createdAt = "2025-01-15T10:30:00Z",
                step = "build",
                status = "FAILED",
            )

        val result = mapper.toDomain(dto)

        assertEquals(CheckpointStatus.FAILED, result.status)
    }

    @Test
    fun `toDomain parses ISO timestamp with milliseconds`() {
        val dto =
            CheckpointDto(
                id = "cp-7",
                pipelineId = "pipe-7",
                createdAt = "2025-01-15T10:30:00.123Z",
                step = "build",
                status = "passed",
            )

        val result = mapper.toDomain(dto)

        assertEquals(1736937000123L, result.createdAt)
    }

    @Test
    fun `toDomain returns 0 for malformed timestamp`() {
        val dto =
            CheckpointDto(
                id = "cp-8",
                pipelineId = "pipe-8",
                createdAt = "not-a-date",
                step = "build",
                status = "passed",
            )

        val result = mapper.toDomain(dto)

        assertEquals(0L, result.createdAt)
    }

    @Test
    fun `toDomain returns 0 for empty timestamp`() {
        val dto =
            CheckpointDto(
                id = "cp-9",
                pipelineId = "pipe-9",
                createdAt = "",
                step = "build",
                status = "passed",
            )

        val result = mapper.toDomain(dto)

        assertEquals(0L, result.createdAt)
    }
}
