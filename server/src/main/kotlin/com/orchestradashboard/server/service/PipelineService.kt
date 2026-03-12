package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.CreatePipelineRunRequest
import com.orchestradashboard.server.model.PatchPipelineRunRequest
import com.orchestradashboard.server.model.PipelineRunEntity
import com.orchestradashboard.server.model.PipelineRunMapper
import com.orchestradashboard.server.model.PipelineRunResponse
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.repository.PipelineRunJpaRepository
import com.orchestradashboard.server.websocket.AgentEventWebSocketHandler
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PipelineService(
    private val pipelineRepository: PipelineRunJpaRepository,
    private val agentRepository: AgentJpaRepository,
    private val mapper: PipelineRunMapper,
    private val webSocketHandler: AgentEventWebSocketHandler,
) {
    companion object {
        val VALID_STATUSES = setOf("QUEUED", "RUNNING", "PASSED", "FAILED", "CANCELLED")
        val TERMINAL_STATUSES = setOf("PASSED", "FAILED", "CANCELLED")
    }

    fun getPipelines(
        agentId: String?,
        status: String?,
        pageable: Pageable,
    ): Page<PipelineRunResponse> {
        val page =
            when {
                agentId != null && status != null -> pipelineRepository.findByAgentIdAndStatus(agentId, status, pageable)
                agentId != null -> pipelineRepository.findByAgentId(agentId, pageable)
                status != null -> pipelineRepository.findByStatus(status, pageable)
                else -> pipelineRepository.findAll(pageable)
            }
        return page.map { mapper.toResponse(it) }
    }

    fun getPipeline(id: String): PipelineRunResponse {
        val entity =
            pipelineRepository.findById(id)
                .orElseThrow { NoSuchElementException("Pipeline run with id '$id' not found") }
        return mapper.toResponse(entity)
    }

    fun createPipeline(request: CreatePipelineRunRequest): PipelineRunResponse {
        agentRepository.findById(request.agentId)
            .orElseThrow { NoSuchElementException("Agent with id '${request.agentId}' not found") }
        val entity =
            PipelineRunEntity(
                id = request.id ?: UUID.randomUUID().toString(),
                agentId = request.agentId,
                pipelineName = request.pipelineName,
                status = "QUEUED",
                steps = mapper.serializeSteps(request.steps),
                startedAt = System.currentTimeMillis(),
                triggerInfo = request.triggerInfo,
            )
        val response = mapper.toResponse(pipelineRepository.save(entity))
        webSocketHandler.broadcastPipelineEvent(response, "PIPELINE_STARTED")
        return response
    }

    fun updateStatus(
        id: String,
        status: String,
    ): PipelineRunResponse {
        val existing =
            pipelineRepository.findById(id)
                .orElseThrow { NoSuchElementException("Pipeline run with id '$id' not found") }
        val finishedAt = if (status in TERMINAL_STATUSES) System.currentTimeMillis() else existing.finishedAt
        val updated = existing.copy(status = status, finishedAt = finishedAt)
        return mapper.toResponse(pipelineRepository.save(updated))
    }

    fun updatePipeline(
        id: String,
        request: PatchPipelineRunRequest,
    ): PipelineRunResponse {
        require(request.status == null || request.status in VALID_STATUSES) {
            "Invalid status '${request.status}'. Valid: ${VALID_STATUSES.joinToString()}"
        }

        val existing =
            pipelineRepository.findById(id)
                .orElseThrow { NoSuchElementException("Pipeline run with id '$id' not found") }

        val newStatus = request.status ?: existing.status
        val newSteps = if (request.steps != null) mapper.serializeSteps(request.steps) else existing.steps
        val newFinishedAt =
            when {
                request.finishedAt != null -> {
                    require(request.finishedAt > 0) { "finished_at must be positive" }
                    request.finishedAt
                }
                newStatus in TERMINAL_STATUSES && existing.finishedAt == null -> System.currentTimeMillis()
                else -> existing.finishedAt
            }

        val updated = existing.copy(status = newStatus, steps = newSteps, finishedAt = newFinishedAt)
        val saved = pipelineRepository.save(updated)
        val response = mapper.toResponse(saved)

        if (request.status != null && request.status in TERMINAL_STATUSES) {
            webSocketHandler.broadcastPipelineEvent(response, "PIPELINE_COMPLETED")
        }
        return response
    }
}
