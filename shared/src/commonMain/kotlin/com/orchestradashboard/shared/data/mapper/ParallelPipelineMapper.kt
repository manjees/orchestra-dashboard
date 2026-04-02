package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.ParallelPipelineGroupDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineDependencyDto
import com.orchestradashboard.shared.domain.model.DependencyType
import com.orchestradashboard.shared.domain.model.ParallelPipelineGroup
import com.orchestradashboard.shared.domain.model.PipelineDependency

class ParallelPipelineMapper(
    private val pipelineMapper: MonitoredPipelineMapper,
) {
    fun mapToParallelGroup(dto: ParallelPipelineGroupDto): ParallelPipelineGroup =
        ParallelPipelineGroup(
            parentPipelineId = dto.parentPipelineId,
            pipelines = dto.lanes.map { pipelineMapper.mapToDomain(it) },
            dependencies = dto.dependencies.map { mapDependency(it) },
        )

    private fun mapDependency(dto: PipelineDependencyDto): PipelineDependency =
        PipelineDependency(
            sourceLaneId = dto.sourceLaneId,
            targetLaneId = dto.targetLaneId,
            type = parseDependencyType(dto.type),
        )

    fun parseDependencyType(type: String): DependencyType =
        when (type.lowercase()) {
            "provides_input" -> DependencyType.PROVIDES_INPUT
            else -> DependencyType.BLOCKS_START
        }
}
