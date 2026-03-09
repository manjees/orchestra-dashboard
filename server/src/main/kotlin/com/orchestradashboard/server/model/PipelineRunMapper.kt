package com.orchestradashboard.server.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component

@Component
class PipelineRunMapper {
    private val objectMapper = jacksonObjectMapper()

    fun toResponse(entity: PipelineRunEntity): PipelineRunResponse =
        PipelineRunResponse(
            id = entity.id,
            agentId = entity.agentId,
            pipelineName = entity.pipelineName,
            status = entity.status,
            steps = deserializeSteps(entity.steps),
            startedAt = entity.startedAt,
            finishedAt = entity.finishedAt,
            triggerInfo = entity.triggerInfo,
        )

    fun toResponseList(entities: List<PipelineRunEntity>): List<PipelineRunResponse> = entities.map { toResponse(it) }

    fun deserializeSteps(json: String): List<PipelineStepResponse> =
        if (json.isBlank()) {
            emptyList()
        } else {
            try {
                objectMapper.readValue(json, object : TypeReference<List<PipelineStepResponse>>() {})
            } catch (_: Exception) {
                emptyList()
            }
        }

    fun serializeSteps(steps: List<PipelineStepResponse>): String = objectMapper.writeValueAsString(steps)
}
