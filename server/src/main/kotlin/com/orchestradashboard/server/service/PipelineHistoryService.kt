package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.PipelineHistoryEntity
import com.orchestradashboard.server.model.PipelineHistoryMapper
import com.orchestradashboard.server.model.PipelineHistoryResponse
import com.orchestradashboard.server.model.PipelineStepHistoryEntity
import com.orchestradashboard.server.repository.PipelineHistoryJpaRepository
import com.orchestradashboard.server.repository.PipelineHistorySpec
import com.orchestradashboard.server.repository.PipelineStepHistoryJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
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
        keyword: String?,
        from: Long?,
        to: Long?,
        pageable: Pageable,
    ): Page<PipelineHistoryResponse> {
        val effectiveKeyword = keyword?.takeIf { it.isNotEmpty() }
        val page = dispatchQuery(project, status, effectiveKeyword, from, to, pageable)
        return page.map { mapper.toResponse(it) }
    }

    private fun dispatchQuery(
        project: String?,
        status: String?,
        keyword: String?,
        from: Long?,
        to: Long?,
        pageable: Pageable,
    ): Page<PipelineHistoryEntity> {
        var spec = Specification.where<PipelineHistoryEntity>(null)
        if (project != null) spec = spec.and(PipelineHistorySpec.byProject(project))
        if (status != null) spec = spec.and(PipelineHistorySpec.byStatus(status))
        if (keyword != null) spec = spec.and(PipelineHistorySpec.byKeyword(keyword))
        if (from != null && to != null) spec = spec.and(PipelineHistorySpec.byDateRange(from, to))
        return historyRepository.findAll(spec, pageable)
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
