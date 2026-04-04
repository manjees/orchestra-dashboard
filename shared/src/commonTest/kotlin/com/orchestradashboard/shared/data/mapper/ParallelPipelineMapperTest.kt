package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.ParallelPipelineGroupDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineDependencyDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineStepDto
import com.orchestradashboard.shared.domain.model.DependencyType
import kotlin.test.Test
import kotlin.test.assertEquals

class ParallelPipelineMapperTest {
    private val pipelineMapper = MonitoredPipelineMapper()
    private val mapper = ParallelPipelineMapper(pipelineMapper)

    @Test
    fun `mapToParallelGroup maps DTO with multiple lanes correctly`() {
        val dto =
            ParallelPipelineGroupDto(
                parentPipelineId = "p1",
                lanes =
                    listOf(
                        makeLaneDto("lane-1", listOf(PipelineStepDto("coding", "running", 1.0))),
                        makeLaneDto("lane-2", listOf(PipelineStepDto("design", "passed", 2.0))),
                    ),
                dependencies = emptyList(),
            )

        val group = mapper.mapToParallelGroup(dto)

        assertEquals("p1", group.parentPipelineId)
        assertEquals(2, group.pipelines.size)
        assertEquals("lane-1", group.pipelines[0].id)
        assertEquals("lane-2", group.pipelines[1].id)
    }

    @Test
    fun `mapToParallelGroup maps dependencies correctly`() {
        val dto =
            ParallelPipelineGroupDto(
                parentPipelineId = "p1",
                lanes =
                    listOf(
                        makeLaneDto("lane-1"),
                        makeLaneDto("lane-2"),
                    ),
                dependencies =
                    listOf(
                        PipelineDependencyDto("lane-1", "lane-2", "blocks_start"),
                        PipelineDependencyDto("lane-1", "lane-3", "provides_input"),
                    ),
            )

        val group = mapper.mapToParallelGroup(dto)

        assertEquals(2, group.dependencies.size)
        assertEquals("lane-1", group.dependencies[0].sourceLaneId)
        assertEquals("lane-2", group.dependencies[0].targetLaneId)
        assertEquals(DependencyType.BLOCKS_START, group.dependencies[0].type)
        assertEquals(DependencyType.PROVIDES_INPUT, group.dependencies[1].type)
    }

    @Test
    fun `mapToParallelGroup with empty lanes returns empty group`() {
        val dto =
            ParallelPipelineGroupDto(
                parentPipelineId = "p1",
                lanes = emptyList(),
                dependencies = emptyList(),
            )

        val group = mapper.mapToParallelGroup(dto)

        assertEquals("p1", group.parentPipelineId)
        assertEquals(0, group.pipelines.size)
        assertEquals(0, group.dependencies.size)
    }

    @Test
    fun `mapToParallelGroup with unknown dependency type defaults to BLOCKS_START`() {
        val dto =
            ParallelPipelineGroupDto(
                parentPipelineId = "p1",
                lanes = listOf(makeLaneDto("lane-1"), makeLaneDto("lane-2")),
                dependencies =
                    listOf(
                        PipelineDependencyDto("lane-1", "lane-2", "unknown_type"),
                    ),
            )

        val group = mapper.mapToParallelGroup(dto)

        assertEquals(DependencyType.BLOCKS_START, group.dependencies[0].type)
    }

    private fun makeLaneDto(
        id: String,
        steps: List<PipelineStepDto> = emptyList(),
    ) = OrchestratorPipelineDto(
        id = id,
        projectName = "test-project",
        issueNum = 1,
        issueTitle = "Test Issue",
        mode = "parallel",
        status = "running",
        currentStep = null,
        startedAt = "2024-06-15T10:00:00Z",
        steps = steps,
        elapsedTotalSec = 0.0,
    )
}
