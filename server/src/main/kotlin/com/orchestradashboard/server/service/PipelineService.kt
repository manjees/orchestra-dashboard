package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.CreatePipelineRunRequest
import com.orchestradashboard.server.model.PipelineRunEntity
import com.orchestradashboard.server.model.PipelineRunMapper
import com.orchestradashboard.server.model.PipelineRunResponse
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.repository.PipelineRunJpaRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PipelineService(
    private val pipelineRepository: PipelineRunJpaRepository,
    private val agentRepository: AgentJpaRepository,
    private val mapper: PipelineRunMapper,
) {
    companion object {
        private val TERMINAL_STATUSES = setOf("PASSED", "FAILED", "CANCELLED")
    }

    fun getAllPipelineRuns(
        agentId: String?,
        status: String?,
        pageable: Pageable,
    ): List<PipelineRunResponse> {
        val page =
            when {
                agentId != null && status != null -> pipelineRepository.findByAgentIdAndStatus(agentId, status, pageable)
                agentId != null -> pipelineRepository.findByAgentId(agentId, pageable)
                status != null -> pipelineRepository.findByStatus(status, pageable)
                else -> pipelineRepository.findAll(pageable)
            }
        return mapper.toResponseList(page.content)
    }

    fun getPipelineRun(id: String): PipelineRunResponse {
        val entity =
            pipelineRepository.findById(id)
                .orElseThrow { NoSuchElementException("PipelineRun with id '$id' not found") }
        return mapper.toResponse(entity)
    }

    fun createPipelineRun(request: CreatePipelineRunRequest): PipelineRunResponse {
        if (!agentRepository.existsById(request.agentId)) {
            throw NoSuchElementException("Agent with id '${request.agentId}' not found")
        }
        val entity =
            PipelineRunEntity(
                id = UUID.randomUUID().toString(),
                agentId = request.agentId,
                pipelineName = request.pipelineName,
                status = "QUEUED",
                steps = mapper.serializeSteps(request.steps),
                startedAt = System.currentTimeMillis(),
                triggerInfo = request.triggerInfo,
            )
        return mapper.toResponse(pipelineRepository.save(entity))
    }

    fun updateStatus(
        id: String,
        status: String,
    ): PipelineRunResponse {
        val existing =
            pipelineRepository.findById(id)
                .orElseThrow { NoSuchElementException("PipelineRun with id '$id' not found") }
        val finishedAt = if (status in TERMINAL_STATUSES) System.currentTimeMillis() else existing.finishedAt
        val updated = existing.copy(status = status, finishedAt = finishedAt)
        return mapper.toResponse(pipelineRepository.save(updated))
    }
}
