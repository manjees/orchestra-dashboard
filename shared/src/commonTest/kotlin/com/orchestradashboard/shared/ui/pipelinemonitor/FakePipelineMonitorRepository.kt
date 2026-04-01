package com.orchestradashboard.shared.ui.pipelinemonitor

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.model.MonitoredStep
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import com.orchestradashboard.shared.domain.repository.PipelineMonitorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

class FakePipelineMonitorRepository : PipelineMonitorRepository {
    var pipelineDetailResult: Result<MonitoredPipeline> = Result.success(defaultPipeline())
    val eventsFlow = MutableSharedFlow<PipelineEventDto>()

    var getPipelineDetailCallCount = 0
        private set

    override suspend fun getPipelineDetail(pipelineId: String): Result<MonitoredPipeline> {
        getPipelineDetailCallCount++
        return pipelineDetailResult
    }

    override fun observePipelineEvents(pipelineId: String): Flow<PipelineEventDto> = eventsFlow.filter { it.pipelineId == pipelineId }

    companion object {
        fun defaultPipeline() =
            MonitoredPipeline(
                id = "p1",
                projectName = "test-project",
                issueNum = 1,
                issueTitle = "Test Issue",
                mode = "sequential",
                status = PipelineRunStatus.RUNNING,
                steps =
                    listOf(
                        MonitoredStep("planning", StepStatus.PASSED, 5000L, null, ""),
                        MonitoredStep("coding", StepStatus.RUNNING, 3000L, 1000L, ""),
                        MonitoredStep("testing", StepStatus.PENDING, 0L, null, ""),
                    ),
                startedAtMs = 1718447400000L,
                elapsedTotalSec = 8.0,
            )
    }
}
