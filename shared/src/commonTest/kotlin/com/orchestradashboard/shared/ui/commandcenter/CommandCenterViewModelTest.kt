package com.orchestradashboard.shared.ui.commandcenter

import com.orchestradashboard.shared.domain.model.CommandResult
import com.orchestradashboard.shared.domain.model.DesignResult
import com.orchestradashboard.shared.domain.model.DiscussResult
import com.orchestradashboard.shared.domain.model.InitProjectRequest
import com.orchestradashboard.shared.domain.model.PlannedIssue
import com.orchestradashboard.shared.domain.model.PlanIssuesResult
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.model.ProjectVisibility
import com.orchestradashboard.shared.domain.model.ShellResult
import com.orchestradashboard.shared.domain.usecase.DesignUseCase
import com.orchestradashboard.shared.domain.usecase.DiscussUseCase
import com.orchestradashboard.shared.domain.usecase.ExecuteShellUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectsUseCase
import com.orchestradashboard.shared.domain.usecase.InitProjectUseCase
import com.orchestradashboard.shared.domain.usecase.PlanIssuesUseCase
import com.orchestradashboard.shared.ui.projectexplorer.FakeProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CommandCenterViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCommandRepository
    private lateinit var fakeProjectRepository: FakeProjectRepository
    private lateinit var viewModel: CommandCenterViewModel

    private val testProject = Project(
        name = "test-project",
        path = "/path/to/project",
        ciCommands = emptyList(),
        openIssuesCount = 0,
        recentSolves = 0,
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCommandRepository()
        fakeProjectRepository = FakeProjectRepository()
        viewModel = CommandCenterViewModel(
            initProjectUseCase = InitProjectUseCase(fakeRepository),
            planIssuesUseCase = PlanIssuesUseCase(fakeRepository),
            discussUseCase = DiscussUseCase(fakeRepository),
            designUseCase = DesignUseCase(fakeRepository),
            executeShellUseCase = ExecuteShellUseCase(fakeRepository),
            getProjectsUseCase = GetProjectsUseCase(fakeProjectRepository),
        )
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default tab and no results and not loading`() {
        val state = viewModel.uiState.value
        assertEquals(CommandTab.INIT, state.activeTab)
        assertNull(state.initResult)
        assertNull(state.planResult)
        assertNull(state.discussResult)
        assertNull(state.designResult)
        assertNull(state.shellResult)
        assertFalse(state.isInitLoading)
        assertFalse(state.isPlanLoading)
        assertFalse(state.isDiscussLoading)
        assertFalse(state.isDesignLoading)
        assertFalse(state.isShellLoading)
        assertNull(state.error)
    }

    @Test
    fun `selectTab updates activeTab`() {
        viewModel.selectTab(CommandTab.SHELL)
        assertEquals(CommandTab.SHELL, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `updateInitName updates initName`() {
        viewModel.updateInitName("my-project")
        assertEquals("my-project", viewModel.uiState.value.initName)
    }

    @Test
    fun `updateInitDescription updates initDescription`() {
        viewModel.updateInitDescription("A description")
        assertEquals("A description", viewModel.uiState.value.initDescription)
    }

    @Test
    fun `updateInitVisibility updates initVisibility`() {
        viewModel.updateInitVisibility(ProjectVisibility.PRIVATE)
        assertEquals(ProjectVisibility.PRIVATE, viewModel.uiState.value.initVisibility)
    }

    @Test
    fun `executeInit sets isInitLoading then populates initResult on success`() = runTest {
        val expectedResult = CommandResult(success = true, message = "Created!", pipelineId = "p-1")
        fakeRepository.initProjectResult = Result.success(expectedResult)

        viewModel.updateInitName("test-project")
        viewModel.executeInit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isInitLoading)
        assertNotNull(state.initResult)
        assertEquals("Created!", state.initResult!!.message)
        assertNull(state.error)
    }

    @Test
    fun `executeInit failure sets error message`() = runTest {
        fakeRepository.initProjectResult = Result.failure(RuntimeException("Init failed"))

        viewModel.updateInitName("test-project")
        viewModel.executeInit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isInitLoading)
        assertNull(state.initResult)
        assertEquals("Init failed", state.error)
    }

    @Test
    fun `executeInit does nothing when initName is blank`() = runTest {
        viewModel.updateInitName("")
        viewModel.executeInit()
        advanceUntilIdle()

        assertEquals(0, fakeRepository.initProjectCallCount)
        assertNull(viewModel.uiState.value.initResult)
    }

    @Test
    fun `executePlan sets isLoading then populates planResult on success`() = runTest {
        val issues = listOf(PlannedIssue("Fix bug", "body", listOf("bug")))
        fakeRepository.planIssuesResult = Result.success(PlanIssuesResult(issues))

        viewModel.selectPlanProject(testProject)
        viewModel.executePlan()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isPlanLoading)
        assertNotNull(state.planResult)
        assertEquals(1, state.planResult!!.issues.size)
        assertEquals("Fix bug", state.planResult!!.issues[0].title)
        assertNull(state.error)
    }

    @Test
    fun `executePlan failure sets error message`() = runTest {
        fakeRepository.planIssuesResult = Result.failure(RuntimeException("Plan failed"))

        viewModel.selectPlanProject(testProject)
        viewModel.executePlan()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isPlanLoading)
        assertNull(state.planResult)
        assertEquals("Plan failed", state.error)
    }

    @Test
    fun `executePlan does nothing when no project selected`() = runTest {
        viewModel.executePlan()
        advanceUntilIdle()

        assertEquals(0, fakeRepository.planIssuesCallCount)
    }

    @Test
    fun `executeDiscuss sets isLoading then populates discussResult on success`() = runTest {
        val answer = DiscussResult(answer = "Great question!", suggestedIssues = emptyList())
        fakeRepository.discussResult = Result.success(answer)

        viewModel.selectDiscussProject(testProject)
        viewModel.updateDiscussQuestion("How does it work?")
        viewModel.executeDiscuss()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDiscussLoading)
        assertNotNull(state.discussResult)
        assertEquals("Great question!", state.discussResult!!.answer)
        assertNull(state.error)
    }

    @Test
    fun `executeDiscuss failure sets error message`() = runTest {
        fakeRepository.discussResult = Result.failure(RuntimeException("Discuss failed"))

        viewModel.selectDiscussProject(testProject)
        viewModel.updateDiscussQuestion("question")
        viewModel.executeDiscuss()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDiscussLoading)
        assertNull(state.discussResult)
        assertEquals("Discuss failed", state.error)
    }

    @Test
    fun `executeDiscuss does nothing when project or question is missing`() = runTest {
        viewModel.executeDiscuss()
        advanceUntilIdle()

        assertEquals(0, fakeRepository.discussCallCount)
    }

    @Test
    fun `executeDesign sets isLoading then populates designResult on success`() = runTest {
        val spec = DesignResult(spec = "UI spec here")
        fakeRepository.designResult = Result.success(spec)

        viewModel.selectDesignProject(testProject)
        viewModel.updateDesignFigmaUrl("https://figma.com/file/abc")
        viewModel.executeDesign()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDesignLoading)
        assertNotNull(state.designResult)
        assertEquals("UI spec here", state.designResult!!.spec)
        assertNull(state.error)
    }

    @Test
    fun `executeDesign failure sets error message`() = runTest {
        fakeRepository.designResult = Result.failure(RuntimeException("Design failed"))

        viewModel.selectDesignProject(testProject)
        viewModel.updateDesignFigmaUrl("https://figma.com/file/abc")
        viewModel.executeDesign()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDesignLoading)
        assertNull(state.designResult)
        assertEquals("Design failed", state.error)
    }

    @Test
    fun `executeDesign does nothing when project or figmaUrl is missing`() = runTest {
        viewModel.executeDesign()
        advanceUntilIdle()

        assertEquals(0, fakeRepository.designCallCount)
    }

    @Test
    fun `executeShell with safe command sets isLoading then populates shellResult on success`() = runTest {
        fakeRepository.shellResult = Result.success(ShellResult(output = "hello world", exitCode = 0))

        viewModel.updateShellCommand("echo hello world")
        viewModel.executeShell()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isShellLoading)
        assertNotNull(state.shellResult)
        assertEquals("hello world", state.shellResult!!.output)
        assertEquals(0, state.shellResult!!.exitCode)
        assertNull(state.error)
    }

    @Test
    fun `executeShell with dangerous command sets showDangerDialog true and does NOT execute`() = runTest {
        viewModel.updateShellCommand("rm -rf /tmp/test")
        viewModel.executeShell()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showDangerDialog)
        assertEquals("rm -rf /tmp/test", state.pendingDangerousCommand)
        assertEquals(0, fakeRepository.executeShellCallCount)
    }

    @Test
    fun `confirmDangerousCommand executes the pending command`() = runTest {
        fakeRepository.shellResult = Result.success(ShellResult(output = "done", exitCode = 0))

        viewModel.updateShellCommand("rm -rf /tmp/test")
        viewModel.executeShell()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showDangerDialog)

        viewModel.confirmDangerousCommand()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showDangerDialog)
        assertNull(state.pendingDangerousCommand)
        assertNotNull(state.shellResult)
        assertEquals(1, fakeRepository.executeShellCallCount)
        assertEquals("rm -rf /tmp/test", fakeRepository.lastShellCommand)
    }

    @Test
    fun `cancelDangerousCommand clears dialog without executing`() = runTest {
        viewModel.updateShellCommand("rm -rf /tmp/test")
        viewModel.executeShell()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showDangerDialog)

        viewModel.cancelDangerousCommand()

        val state = viewModel.uiState.value
        assertFalse(state.showDangerDialog)
        assertNull(state.pendingDangerousCommand)
        assertEquals(0, fakeRepository.executeShellCallCount)
    }

    @Test
    fun `executeShell failure sets error message`() = runTest {
        fakeRepository.shellResult = Result.failure(RuntimeException("Shell failed"))

        viewModel.updateShellCommand("ls -la")
        viewModel.executeShell()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isShellLoading)
        assertNull(state.shellResult)
        assertEquals("Shell failed", state.error)
    }

    @Test
    fun `executeShell does nothing when command is blank`() = runTest {
        viewModel.updateShellCommand("")
        viewModel.executeShell()
        advanceUntilIdle()

        assertEquals(0, fakeRepository.executeShellCallCount)
    }

    @Test
    fun `clearError resets error to null`() = runTest {
        fakeRepository.initProjectResult = Result.failure(RuntimeException("error"))
        viewModel.updateInitName("test")
        viewModel.executeInit()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }
}
