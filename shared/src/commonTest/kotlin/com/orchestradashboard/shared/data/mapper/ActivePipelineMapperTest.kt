package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineStepDto
import kotlin.test.Test
import kotlin.test.assertEquals

class ActivePipelineMapperTest {
    private val mapper = ActivePipelineMapper()

    private fun createPipelineDto(
        id: String = "pipe-1",
        projectName: String = "my-project",
        issueNum: Int = 42,
        issueTitle: String = "Fix login bug",
        currentStep: String? = "building",
        elapsedTotalSec: Double = 120.5,
        status: String = "RUNNING",
    ) = OrchestratorPipelineDto(
        id = id,
        projectName = projectName,
        issueNum = issueNum,
        issueTitle = issueTitle,
        mode = "solve",
        status = status,
        currentStep = currentStep,
        startedAt = "2024-01-01T00:00:00Z",
        steps = listOf(PipelineStepDto("build", "RUNNING", 60.0)),
        elapsedTotalSec = elapsedTotalSec,
    )

    @Test
    fun `maps OrchestratorPipelineDto to ActivePipeline with all fields`() {
        val dto = createPipelineDto()
        val result = mapper.toDomain(dto)

        assertEquals("pipe-1", result.id)
        assertEquals("my-project", result.projectName)
        assertEquals(42, result.issueNum)
        assertEquals("Fix login bug", result.issueTitle)
        assertEquals("building", result.currentStep)
        assertEquals(120.5, result.elapsedTotalSec)
        assertEquals("RUNNING", result.status)
    }

    @Test
    fun `maps null currentStep to empty string`() {
        val dto = createPipelineDto(currentStep = null)
        val result = mapper.toDomain(dto)

        assertEquals("", result.currentStep)
    }

    @Test
    fun `maps list of pipeline DTOs to domain models`() {
        val dtos =
            listOf(
                createPipelineDto(id = "pipe-1"),
                createPipelineDto(id = "pipe-2"),
            )
        val results = mapper.toDomainList(dtos)

        assertEquals(2, results.size)
        assertEquals("pipe-1", results[0].id)
        assertEquals("pipe-2", results[1].id)
    }

    @Test
    fun `maps empty list to empty list`() {
        val results = mapper.toDomainList(emptyList())
        assertEquals(0, results.size)
    }
}
