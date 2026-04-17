package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.OrchestratorNetworkException
import com.orchestradashboard.shared.data.api.OrchestratorNotFoundException
import com.orchestradashboard.shared.data.dto.AgentCommandDto
import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.dto.AgentPageDto
import com.orchestradashboard.shared.data.dto.AggregatedMetricDto
import com.orchestradashboard.shared.data.dto.AuthResponseDto
import com.orchestradashboard.shared.data.dto.PipelineRunDto
import com.orchestradashboard.shared.data.dto.analytics.AnalyticsSummaryDto
import com.orchestradashboard.shared.data.dto.analytics.DurationTrendDto
import com.orchestradashboard.shared.data.dto.analytics.StepFailureDto
import com.orchestradashboard.shared.data.dto.orchestrator.ApprovalRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.ParallelPipelineGroupDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import com.orchestradashboard.shared.data.mapper.CheckpointMapper
import com.orchestradashboard.shared.data.mapper.IssueMapper
import com.orchestradashboard.shared.data.mapper.ProjectMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
class FakeProjectDashboardApi(
    private val projects: List<ProjectDto> = emptyList(),
    private val issues: Map<String, List<OrchestratorIssueDto>> = emptyMap(),
    private val checkpoints: List<CheckpointDto> = emptyList(),
    private val shouldThrow: Exception? = null,
) : DashboardApi {
    var lastIssuePage: Int = -1
    var lastIssuePageSize: Int = -1

    override suspend fun getProjects(): List<ProjectDto> {
        shouldThrow?.let { throw it }
        return projects
    }

    override suspend fun getProject(name: String): ProjectDetailDto = throw NotImplementedError()

    override suspend fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): List<OrchestratorIssueDto> {
        shouldThrow?.let { throw it }
        lastIssuePage = page
        lastIssuePageSize = pageSize
        return issues[name] ?: throw OrchestratorNotFoundException("Project $name not found")
    }

    override suspend fun getCheckpoints(): List<CheckpointDto> {
        shouldThrow?.let { throw it }
        return checkpoints
    }

    override suspend fun retryCheckpoint(checkpointId: String): CheckpointDto = throw NotImplementedError()

    override suspend fun getSystemStatus(): SystemStatusDto = throw NotImplementedError()

    override suspend fun getActivePipelines(): List<OrchestratorPipelineDto> = throw NotImplementedError()

    override suspend fun getActivePipeline(id: String): OrchestratorPipelineDto = throw NotImplementedError()

    override suspend fun getPipelineHistory(): List<PipelineHistoryDto> = throw NotImplementedError()

    override suspend fun getParallelPipelines(parentId: String): ParallelPipelineGroupDto = throw NotImplementedError()

    override suspend fun postSolve(request: SolveCommandRequestDto): SolveCommandResponseDto = throw NotImplementedError()

    override suspend fun postInitProject(request: InitProjectRequestDto): InitProjectResponseDto = throw NotImplementedError()

    override suspend fun postPlanIssues(projectName: String): PlanIssuesResponseDto = throw NotImplementedError()

    override suspend fun postDiscuss(request: DiscussRequestDto): DiscussResponseDto = throw NotImplementedError()

    override suspend fun postDesign(request: DesignRequestDto): DesignResponseDto = throw NotImplementedError()

    override suspend fun postShell(request: ShellRequestDto): ShellResponseDto = throw NotImplementedError()

    override suspend fun respondToApproval(
        approvalId: String,
        request: ApprovalRequestDto,
    ) = throw NotImplementedError()

    override suspend fun getAnalyticsSummary(
        project: String,
        from: Long?,
        to: Long?,
    ): AnalyticsSummaryDto = throw NotImplementedError()

    override suspend fun getStepFailures(project: String): List<StepFailureDto> = throw NotImplementedError()

    override suspend fun getDurationTrends(
        project: String,
        granularity: String,
    ): List<DurationTrendDto> = throw NotImplementedError()

    override fun observeAgents(): Flow<List<AgentDto>> = emptyFlow()

    override suspend fun getAgents(): List<AgentDto> = emptyList()

    override suspend fun getAgent(agentId: String): AgentDto = throw NotImplementedError()

    override suspend fun getPipelineRuns(agentId: String?): List<PipelineRunDto> = emptyList()

    override suspend fun getPipelineRun(runId: String): PipelineRunDto = throw NotImplementedError()

    override suspend fun getRecentEvents(
        agentId: String?,
        limit: Int,
    ): List<AgentEventDto> = emptyList()

    override fun observePipelineRuns(agentId: String?): Flow<List<PipelineRunDto>> = emptyFlow()

    override fun observeEvents(agentId: String): Flow<List<AgentEventDto>> = emptyFlow()

    override suspend fun registerAgent(agent: AgentDto): AgentDto = throw NotImplementedError()

    override suspend fun deregisterAgent(agentId: String) = throw NotImplementedError()

    override suspend fun getAgentsPaged(
        page: Int,
        pageSize: Int,
        status: String?,
    ): AgentPageDto = throw NotImplementedError()

    override suspend fun login(apiKey: String): AuthResponseDto = throw NotImplementedError()

    override suspend fun refreshToken(refreshToken: String): AuthResponseDto = throw NotImplementedError()

    override suspend fun sendCommand(
        agentId: String,
        commandType: String,
    ): AgentCommandDto = throw NotImplementedError()

    override suspend fun getCommands(
        agentId: String,
        limit: Int,
    ): List<AgentCommandDto> = emptyList()

    override suspend fun getAggregatedMetrics(
        agentId: String,
        startTime: Long,
        endTime: Long,
    ): List<AggregatedMetricDto> = emptyList()
}

class ProjectRepositoryImplTest {
    private val projectMapper = ProjectMapper()
    private val issueMapper = IssueMapper()
    private val checkpointMapper = CheckpointMapper()

    private fun createRepository(api: FakeProjectDashboardApi) = ProjectRepositoryImpl(api, projectMapper, issueMapper, checkpointMapper)

    @Test
    fun `getProjects returns mapped domain models`() =
        runTest {
            val api =
                FakeProjectDashboardApi(
                    projects =
                        listOf(
                            ProjectDto("proj-a", "/a", listOf("pytest"), 3, 1),
                            ProjectDto("proj-b", "/b", emptyList(), 0, 0),
                        ),
                )
            val repo = createRepository(api)
            val result = repo.getProjects()
            assertTrue(result.isSuccess)
            val projects = result.getOrThrow()
            assertEquals(2, projects.size)
            assertEquals("proj-a", projects[0].name)
            assertEquals("proj-b", projects[1].name)
        }

    @Test
    fun `getProjects returns failure on network error`() =
        runTest {
            val api = FakeProjectDashboardApi(shouldThrow = OrchestratorNetworkException("timeout"))
            val repo = createRepository(api)
            val result = repo.getProjects()
            assertTrue(result.isFailure)
        }

    @Test
    fun `getProjectIssues returns mapped issues for project`() =
        runTest {
            val api =
                FakeProjectDashboardApi(
                    issues =
                        mapOf(
                            "my-proj" to
                                listOf(
                                    OrchestratorIssueDto(1, "Bug", listOf("bug"), "open", "2025-01-01T00:00:00Z"),
                                ),
                        ),
                )
            val repo = createRepository(api)
            val result = repo.getProjectIssues("my-proj")
            assertTrue(result.isSuccess)
            val issues = result.getOrThrow()
            assertEquals(1, issues.size)
            assertEquals("Bug", issues[0].title)
        }

    @Test
    fun `getProjectIssues passes page and pageSize to API`() =
        runTest {
            val api =
                FakeProjectDashboardApi(
                    issues =
                        mapOf(
                            "proj" to listOf(OrchestratorIssueDto(1, "T", emptyList(), "open", "2025-01-01T00:00:00Z")),
                        ),
                )
            val repo = createRepository(api)
            repo.getProjectIssues("proj", page = 2, pageSize = 10)
            assertEquals(2, api.lastIssuePage)
            assertEquals(10, api.lastIssuePageSize)
        }

    @Test
    fun `getProjectIssues returns failure on 404`() =
        runTest {
            val api = FakeProjectDashboardApi(issues = emptyMap())
            val repo = createRepository(api)
            val result = repo.getProjectIssues("nonexistent")
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is OrchestratorNotFoundException)
        }

    @Test
    fun `getCheckpoints returns mapped checkpoints`() =
        runTest {
            val api =
                FakeProjectDashboardApi(
                    checkpoints =
                        listOf(
                            CheckpointDto("cp-1", "pipe-1", "2025-03-01T00:00:00Z", "test", "failed"),
                        ),
                )
            val repo = createRepository(api)
            val result = repo.getCheckpoints()
            assertTrue(result.isSuccess)
            val checkpoints = result.getOrThrow()
            assertEquals(1, checkpoints.size)
            assertEquals(CheckpointStatus.FAILED, checkpoints[0].status)
        }

    @Test
    fun `getCheckpoints returns failure on network error`() =
        runTest {
            val api = FakeProjectDashboardApi(shouldThrow = OrchestratorNetworkException("offline"))
            val repo = createRepository(api)
            val result = repo.getCheckpoints()
            assertTrue(result.isFailure)
        }
}
