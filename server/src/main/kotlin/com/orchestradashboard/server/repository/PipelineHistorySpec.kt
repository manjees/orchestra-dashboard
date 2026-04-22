package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.PipelineHistoryEntity
import org.springframework.data.jpa.domain.Specification

object PipelineHistorySpec {
    fun byProject(project: String): Specification<PipelineHistoryEntity> =
        Specification { root, _, cb -> cb.equal(root.get<String>("projectName"), project) }

    fun byStatus(status: String): Specification<PipelineHistoryEntity> =
        Specification { root, _, cb -> cb.equal(root.get<String>("status"), status) }

    fun byKeyword(keyword: String): Specification<PipelineHistoryEntity> =
        Specification { root, _, cb ->
            cb.like(cb.lower(root.get("issueTitle")), "%${keyword.lowercase()}%")
        }

    fun byDateRange(from: Long, to: Long): Specification<PipelineHistoryEntity> =
        Specification { root, _, cb -> cb.between(root.get("startedAt"), from, to) }
}
