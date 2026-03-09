package com.orchestradashboard.server.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.stereotype.Component

@Component
class PipelineRunMapper {
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    fun toResponse(entity: PipelineRunEntity): PipelineRunResponse =
        PipelineRunResponse(
            id = entity.id,
            agentId = entity.agentId,
            pipelineName = entity.pipelineName,
            status = entity.status,
            steps = parseSteps(entity.steps),
            startedAt = entity.startedAt,
            finishedAt = entity.finishedAt,
            triggerInfo = entity.triggerInfo,
        )

    fun parseSteps(json: String): List<PipelineStepResponse> =
        if (json.isBlank()) {
            emptyList()
        } else {
            try {
                objectMapper.readValue(json, object : TypeReference<List<PipelineStepResponse>>() {})
            } catch (_: Exception) {
                emptyList()
            }
        }

    fun serializeSteps(steps: List<PipelineStepRequest>): String = objectMapper.writeValueAsString(steps)
}
