package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.OrchestratorApi
import com.orchestradashboard.shared.data.api.OrchestratorNetworkException
import com.orchestradashboard.shared.data.api.OrchestratorNotFoundException
import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import com.orchestradashboard.shared.data.mapper.CheckpointMapper
import com.orchestradashboard.shared.data.mapper.IssueMapper
import com.orchestradashboard.shared.data.mapper.ProjectMapper
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeOrchestratorApi(
    private val projects: List<ProjectDto> = emptyList(),
    private val issues: Map<String, List<OrchestratorIssueDto>> = emptyMap(),
    private val checkpoints: List<CheckpointDto> = emptyList(),
    private val shouldThrow: Exception? = null,
) : OrchestratorApi {
    var lastIssuePage: Int = -1
    var lastIssuePageSize: Int = -1

    override suspend fun getStatus(): SystemStatusDto = throw NotImplementedError()

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

    override suspend fun getPipelines(): List<OrchestratorPipelineDto> = throw NotImplementedError()

    override suspend fun getPipeline(id: String): OrchestratorPipelineDto = throw NotImplementedError()

    override suspend fun getCheckpoints(): List<CheckpointDto> {
        shouldThrow?.let { throw it }
        return checkpoints
    }

    override suspend fun retryCheckpoint(checkpointId: String): CheckpointDto = throw NotImplementedError()

    override suspend fun getPipelineHistory(): List<PipelineHistoryDto> = throw NotImplementedError()

    override fun connectEvents(): Flow<PipelineEventDto> = emptyFlow()
}

class ProjectRepositoryImplTest {
    private val projectMapper = ProjectMapper()
    private val issueMapper = IssueMapper()
    private val checkpointMapper = CheckpointMapper()

    private fun createRepository(api: FakeOrchestratorApi) = ProjectRepositoryImpl(api, projectMapper, issueMapper, checkpointMapper)

    @Test
    fun `getProjects returns mapped domain models`() =
        runTest {
            val api =
                FakeOrchestratorApi(
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
            val api = FakeOrchestratorApi(shouldThrow = OrchestratorNetworkException("timeout"))
            val repo = createRepository(api)
            val result = repo.getProjects()
            assertTrue(result.isFailure)
        }

    @Test
    fun `getProjectIssues returns mapped issues for project`() =
        runTest {
            val api =
                FakeOrchestratorApi(
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
                FakeOrchestratorApi(
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
            val api = FakeOrchestratorApi(issues = emptyMap())
            val repo = createRepository(api)
            val result = repo.getProjectIssues("nonexistent")
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is OrchestratorNotFoundException)
        }

    @Test
    fun `getCheckpoints returns mapped checkpoints`() =
        runTest {
            val api =
                FakeOrchestratorApi(
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
            val api = FakeOrchestratorApi(shouldThrow = OrchestratorNetworkException("offline"))
            val repo = createRepository(api)
            val result = repo.getCheckpoints()
            assertTrue(result.isFailure)
        }
}
