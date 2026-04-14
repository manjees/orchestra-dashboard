package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.FakeOrchestratorApiClient
import com.orchestradashboard.shared.data.api.OrchestratorNetworkException
import com.orchestradashboard.shared.data.dto.orchestrator.DesignResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlannedIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import com.orchestradashboard.shared.data.mapper.CommandMapper
import com.orchestradashboard.shared.domain.model.InitProjectRequest
import com.orchestradashboard.shared.domain.model.ProjectVisibility
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CommandRepositoryImplTest {
    private val fakeApi = FakeOrchestratorApiClient()
    private val mapper = CommandMapper()
    private val repository = CommandRepositoryImpl(fakeApi, mapper)

    private val initRequest =
        InitProjectRequest(
            name = "test-project",
            description = "A project",
            visibility = ProjectVisibility.PUBLIC,
        )

    @Test
    fun `initProject calls api postInitProject and maps result`() =
        runTest {
            fakeApi.initProjectResult = InitProjectResponseDto(success = true, message = "Done", pipelineId = "p-1")

            val result = repository.initProject(initRequest)

            assertTrue(result.isSuccess)
            assertEquals(1, fakeApi.postInitProjectCallCount)
            assertEquals("Done", result.getOrNull()?.message)
        }

    @Test
    fun `initProject failure wraps exception in Result failure`() =
        runTest {
            fakeApi.errorToThrow = OrchestratorNetworkException("Network error")

            val result = repository.initProject(initRequest)

            assertTrue(result.isFailure)
            assertNotNull(result.exceptionOrNull())
        }

    @Test
    fun `planIssues calls api postPlanIssues and returns mapped issue list`() =
        runTest {
            fakeApi.planIssuesResult =
                PlanIssuesResponseDto(
                    issues = listOf(PlannedIssueDto("Issue 1", "body", listOf("bug"))),
                )

            val result = repository.planIssues("test-project")

            assertTrue(result.isSuccess)
            assertEquals(1, fakeApi.postPlanIssuesCallCount)
            assertEquals(1, result.getOrNull()?.issues?.size)
        }

    @Test
    fun `discuss calls api postDiscuss and returns mapped answer`() =
        runTest {
            fakeApi.discussResult = DiscussResponseDto(answer = "The answer", suggestedIssues = emptyList())

            val result = repository.discuss("test-project", "question?")

            assertTrue(result.isSuccess)
            assertEquals(1, fakeApi.postDiscussCallCount)
            assertEquals("The answer", result.getOrNull()?.answer)
        }

    @Test
    fun `design calls api postDesign and returns mapped spec`() =
        runTest {
            fakeApi.designResult = DesignResponseDto(spec = "UI layout", suggestedIssues = emptyList())

            val result = repository.design("test-project", "https://figma.com/file/abc")

            assertTrue(result.isSuccess)
            assertEquals(1, fakeApi.postDesignCallCount)
            assertEquals("UI layout", result.getOrNull()?.spec)
        }

    @Test
    fun `executeShell calls api postShell and returns mapped output`() =
        runTest {
            fakeApi.shellResult = ShellResponseDto(output = "output text", exitCode = 0)

            val result = repository.executeShell("ls -la")

            assertTrue(result.isSuccess)
            assertEquals(1, fakeApi.postShellCallCount)
            assertEquals("output text", result.getOrNull()?.output)
        }

    @Test
    fun `executeShell failure returns Result failure`() =
        runTest {
            fakeApi.errorToThrow = OrchestratorNetworkException("Connection refused")

            val result = repository.executeShell("ls")

            assertTrue(result.isFailure)
        }
}
