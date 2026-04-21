package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.PipelineHistoryEntity
import com.orchestradashboard.server.model.PipelineHistoryMapper
import com.orchestradashboard.server.model.PipelineHistoryResponse
import com.orchestradashboard.server.model.PipelineStepHistoryEntity
import com.orchestradashboard.server.repository.PipelineHistoryJpaRepository
import com.orchestradashboard.server.repository.PipelineStepHistoryJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PipelineHistoryService(
    private val historyRepository: PipelineHistoryJpaRepository,
    private val stepRepository: PipelineStepHistoryJpaRepository,
    private val mapper: PipelineHistoryMapper,
) {
    fun getHistory(
        project: String?,
        status: String?,
        pageable: Pageable,
    ): Page<PipelineHistoryResponse> {
        val page =
            when {
                project != null && status != null ->
                    historyRepository.findByProjectNameAndStatus(project, status, pageable)
                project != null -> historyRepository.findByProjectName(project, pageable)
                status != null -> historyRepository.findByStatus(status, pageable)
                else -> historyRepository.findAll(pageable)
            }
        return page.map { mapper.toResponse(it) }
    }

    fun getHistoryById(id: String): PipelineHistoryResponse {
        val entity =
            historyRepository.findById(id)
                .orElseThrow { NoSuchElementException("Pipeline history with id '$id' not found") }
        return mapper.toResponse(entity)
    }

    fun saveHistory(entity: PipelineHistoryEntity): PipelineHistoryEntity = historyRepository.save(entity)

    fun saveStep(entity: PipelineStepHistoryEntity): PipelineStepHistoryEntity = stepRepository.save(entity)

    fun findHistoryByProjectAndDateRange(
        projectName: String,
        from: Long,
        to: Long,
    ): List<PipelineHistoryEntity> = historyRepository.findByProjectNameAndStartedAtBetween(projectName, from, to)
}
