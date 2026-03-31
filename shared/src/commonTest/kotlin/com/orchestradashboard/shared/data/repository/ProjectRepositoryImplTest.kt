package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.FakeOrchestratorApiClient
import com.orchestradashboard.shared.data.api.OrchestratorNetworkException
import com.orchestradashboard.shared.data.api.OrchestratorNotFoundException
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.mapper.ProjectMapper
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectRepositoryImplTest {
    private val fakeApi = FakeOrchestratorApiClient()
    private val mapper = ProjectMapper()
    private val repository = ProjectRepositoryImpl(fakeApi, mapper)

    @Test
    fun `getProjects returns mapped project list from API`() =
        runTest {
            fakeApi.projectsResult =
                listOf(
                    ProjectDto("proj-1", "/path/1", listOf("make"), 3, 1),
                    ProjectDto("proj-2", "/path/2", listOf("gradle"), 0, 0),
                )

            val result = repository.getProjects()

            assertTrue(result.isSuccess)
            val projects = result.getOrThrow()
            assertEquals(2, projects.size)
            assertEquals("proj-1", projects[0].name)
            assertEquals("/path/1", projects[0].path)
            assertEquals(3, projects[0].openIssuesCount)
            assertEquals(1, projects[0].recentSolves)
        }

    @Test
    fun `getProjects returns failure on network error`() =
        runTest {
            fakeApi.errorToThrow = OrchestratorNetworkException("Connection refused")

            val result = repository.getProjects()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is OrchestratorNetworkException)
        }

    @Test
    fun `getProjectIssues returns mapped issue list from API`() =
        runTest {
            fakeApi.projectIssuesResult =
                listOf(
                    OrchestratorIssueDto(1, "Bug fix", listOf("bug"), "open", "2024-01-15T10:00:00Z"),
                    OrchestratorIssueDto(2, "Feature", listOf("enhancement"), "open", "2024-01-16T10:00:00Z"),
                )

            val result = repository.getProjectIssues("my-project")

            assertTrue(result.isSuccess)
            val issues = result.getOrThrow()
            assertEquals(2, issues.size)
            assertEquals(1, issues[0].number)
            assertEquals("Bug fix", issues[0].title)
        }

    @Test
    fun `getProjectIssues returns failure on network error`() =
        runTest {
            fakeApi.errorToThrow = OrchestratorNetworkException("Timeout")

            val result = repository.getProjectIssues("any-project")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is OrchestratorNetworkException)
        }

    @Test
    fun `getProjectIssues returns failure on 404`() =
        runTest {
            fakeApi.errorToThrow = OrchestratorNotFoundException("Project not found")

            val result = repository.getProjectIssues("nonexistent")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is OrchestratorNotFoundException)
        }
}
