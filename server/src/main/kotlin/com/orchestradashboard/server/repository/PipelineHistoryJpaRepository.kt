package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.PipelineHistoryEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface PipelineHistoryJpaRepository :
    JpaRepository<PipelineHistoryEntity, String>,
    JpaSpecificationExecutor<PipelineHistoryEntity> {
    fun findByProjectName(
        projectName: String,
        pageable: Pageable,
    ): Page<PipelineHistoryEntity>

    fun findByStatus(
        status: String,
        pageable: Pageable,
    ): Page<PipelineHistoryEntity>

    fun findByProjectNameAndStatus(
        projectName: String,
        status: String,
        pageable: Pageable,
    ): Page<PipelineHistoryEntity>

    fun findByStartedAtBetween(
        from: Long,
        to: Long,
        pageable: Pageable,
    ): Page<PipelineHistoryEntity>

    fun findByProjectNameAndStartedAtBetween(
        projectName: String,
        from: Long,
        to: Long,
    ): List<PipelineHistoryEntity>

    fun findByIssueTitleContainingIgnoreCase(
        keyword: String,
        pageable: Pageable,
    ): Page<PipelineHistoryEntity>

    fun findByProjectNameAndIssueTitleContainingIgnoreCase(
        projectName: String,
        keyword: String,
        pageable: Pageable,
    ): Page<PipelineHistoryEntity>

    fun findByStatusAndIssueTitleContainingIgnoreCase(
        status: String,
        keyword: String,
        pageable: Pageable,
    ): Page<PipelineHistoryEntity>

    fun findByProjectNameAndStatusAndIssueTitleContainingIgnoreCase(
        projectName: String,
        status: String,
        keyword: String,
        pageable: Pageable,
    ): Page<PipelineHistoryEntity>
}
