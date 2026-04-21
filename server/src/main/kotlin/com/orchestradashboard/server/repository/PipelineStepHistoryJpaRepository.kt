package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.PipelineStepHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PipelineStepHistoryJpaRepository : JpaRepository<PipelineStepHistoryEntity, String> {
    fun findByPipelineHistoryId(pipelineHistoryId: String): List<PipelineStepHistoryEntity>

    fun findByPipelineHistoryIdIn(pipelineHistoryIds: List<String>): List<PipelineStepHistoryEntity>
}
