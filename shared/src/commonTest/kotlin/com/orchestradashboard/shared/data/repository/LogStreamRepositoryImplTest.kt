package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.OrchestratorApi
import com.orchestradashboard.shared.data.dto.orchestrator.ApprovalRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.LogEntryDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.ParallelPipelineGroupDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import com.orchestradashboard.shared.domain.model.LogLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LogStreamRepositoryImplTest {
    @Test
    fun `observeLogStream maps LogEntryDto to LogEntry domain model`() =
        runTest {
            val dto =
                LogEntryDto(
                    timestamp = "2026-04-23T10:00:00Z",
                    level = "ERROR",
                    message = "boom",
                    stepId = "step-99",
                )
            val api = FakeOrchestratorApiForLogStream(logEntries = listOf(dto))
            val repo = LogStreamRepositoryImpl(api)

            val result = repo.observeLogStream("step-fallback").take(1).toList()

            assertEquals(1, result.size)
            val entry = result[0]
            assertEquals("2026-04-23T10:00:00Z", entry.timestamp)
            assertEquals(LogLevel.ERROR, entry.level)
            assertEquals("boom", entry.message)
            assertEquals("step-99", entry.stepId)
        }

    @Test
    fun `observeLogStream passes stepId to API`() =
        runTest {
            val dto = LogEntryDto(message = "hi", stepId = "step-targeted")
            val api = FakeOrchestratorApiForLogStream(logEntries = listOf(dto))
            val repo = LogStreamRepositoryImpl(api)

            repo.observeLogStream("step-targeted").take(1).toList()

            assertEquals("step-targeted", api.lastStepId)
        }

    @Test
    fun `observeLogStream propagates WebSocket errors`() =
        runTest {
            val boom = RuntimeException("ws closed")
            val api = FakeOrchestratorApiForLogStream(errorToThrow = boom)
            val repo = LogStreamRepositoryImpl(api)

            val thrown =
                assertFailsWith<RuntimeException> {
                    repo.observeLogStream("step-1").take(1).toList()
                }
            assertEquals("ws closed", thrown.message)
        }

    @Test
    fun `multiple LogEntryDto emissions produce multiple LogEntry items`() =
        runTest {
            val dtos =
                listOf(
                    LogEntryDto(timestamp = "t1", level = "INFO", message = "a", stepId = "s"),
                    LogEntryDto(timestamp = "t2", level = "WARN", message = "b", stepId = "s"),
                    LogEntryDto(timestamp = "t3", level = "DEBUG", message = "c", stepId = "s"),
                )
            val api = FakeOrchestratorApiForLogStream(logEntries = dtos)
            val repo = LogStreamRepositoryImpl(api)

            val result = repo.observeLogStream("s").take(3).toList()

            assertEquals(3, result.size)
            assertEquals("a", result[0].message)
            assertEquals(LogLevel.INFO, result[0].level)
            assertEquals("b", result[1].message)
            assertEquals(LogLevel.WARN, result[1].level)
            assertEquals("c", result[2].message)
            assertEquals(LogLevel.DEBUG, result[2].level)
            assertTrue(result.all { it.stepId == "s" })
        }
}

@Suppress("TooManyFunctions")
private class FakeOrchestratorApiForLogStream(
    private val logEntries: List<LogEntryDto> = emptyList(),
    private val errorToThrow: Throwable? = null,
) : OrchestratorApi {
    var lastStepId: String? = null
        private set

    override fun connectLogStream(stepId: String): Flow<LogEntryDto> {
        lastStepId = stepId
        return if (errorToThrow != null) {
            flow { throw errorToThrow }
        } else {
            logEntries.asFlow()
        }
    }

    override fun connectEvents(): Flow<PipelineEventDto> = throw NotImplementedError()

    override fun connectEvents(pipelineId: String): Flow<PipelineEventDto> = throw NotImplementedError()

    override suspend fun getStatus(): SystemStatusDto = throw NotImplementedError()

    override suspend fun getProjects(): List<ProjectDto> = throw NotImplementedError()

    override suspend fun getProject(name: String): ProjectDetailDto = throw NotImplementedError()

    override suspend fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): List<OrchestratorIssueDto> = throw NotImplementedError()

    override suspend fun getPipelines(): List<OrchestratorPipelineDto> = throw NotImplementedError()

    override suspend fun getPipeline(id: String): OrchestratorPipelineDto = throw NotImplementedError()

    override suspend fun getCheckpoints(): List<CheckpointDto> = throw NotImplementedError()

    override suspend fun retryCheckpoint(checkpointId: String): CheckpointDto = throw NotImplementedError()

    override suspend fun postSolve(request: SolveCommandRequestDto): SolveCommandResponseDto = throw NotImplementedError()

    override suspend fun getPipelineHistory(): List<PipelineHistoryDto> = throw NotImplementedError()

    override suspend fun getParallelPipelines(parentId: String): ParallelPipelineGroupDto = throw NotImplementedError()

    override suspend fun postInitProject(request: InitProjectRequestDto): InitProjectResponseDto = throw NotImplementedError()

    override suspend fun postPlanIssues(projectName: String): PlanIssuesResponseDto = throw NotImplementedError()

    override suspend fun postDiscuss(request: DiscussRequestDto): DiscussResponseDto = throw NotImplementedError()

    override suspend fun postDesign(request: DesignRequestDto): DesignResponseDto = throw NotImplementedError()

    override suspend fun postShell(request: ShellRequestDto): ShellResponseDto = throw NotImplementedError()

    override suspend fun respondToApproval(
        approvalId: String,
        request: ApprovalRequestDto,
    ) = throw NotImplementedError()
}
