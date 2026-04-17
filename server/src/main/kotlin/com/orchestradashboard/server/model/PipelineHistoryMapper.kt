package com.orchestradashboard.server.model

import org.springframework.stereotype.Component

@Component("serverPipelineHistoryMapper")
class PipelineHistoryMapper {
    fun toResponse(entity: PipelineHistoryEntity): PipelineHistoryResponse =
        PipelineHistoryResponse(
            id = entity.id,
            projectName = entity.projectName,
            issueNum = entity.issueNum,
            issueTitle = entity.issueTitle,
            mode = entity.mode,
            status = entity.status,
            startedAt = entity.startedAt,
            completedAt = entity.completedAt,
            elapsedTotalSec = entity.elapsedTotalSec,
            prUrl = entity.prUrl,
            steps = entity.steps.map { toStepResponse(it) },
        )

    fun toStepResponse(entity: PipelineStepHistoryEntity): StepHistoryResponse =
        StepHistoryResponse(
            stepName = entity.stepName,
            status = entity.status,
            elapsedSec = entity.elapsedSec,
            failDetail = entity.failDetail,
        )
}
