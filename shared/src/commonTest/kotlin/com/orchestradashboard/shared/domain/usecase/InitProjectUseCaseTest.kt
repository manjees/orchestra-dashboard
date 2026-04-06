package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.CommandResult
import com.orchestradashboard.shared.domain.model.InitProjectRequest
import com.orchestradashboard.shared.domain.model.ProjectVisibility
import com.orchestradashboard.shared.ui.commandcenter.FakeCommandRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InitProjectUseCaseTest {
    private val repository = FakeCommandRepository()
    private val useCase = InitProjectUseCase(repository)

    private val request = InitProjectRequest(
        name = "my-project",
        description = "A test project",
        visibility = ProjectVisibility.PUBLIC,
    )

    @Test
    fun `invoke with valid params returns Result success with command result`() = runTest {
        val expected = CommandResult(success = true, message = "Project created")
        repository.initProjectResult = Result.success(expected)

        val result = useCase(request)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke when repository throws returns Result failure`() = runTest {
        repository.initProjectResult = Result.failure(RuntimeException("Network error"))

        val result = useCase(request)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke passes PUBLIC visibility to repository`() = runTest {
        val publicRequest = InitProjectRequest(
            name = "public-project",
            description = "A public project",
            visibility = ProjectVisibility.PUBLIC,
        )
        repository.initProjectResult = Result.success(CommandResult(success = true, message = "Created"))

        useCase(publicRequest)

        assertEquals(ProjectVisibility.PUBLIC, repository.lastInitRequest?.visibility)
    }

    @Test
    fun `invoke passes PRIVATE visibility to repository`() = runTest {
        val privateRequest = InitProjectRequest(
            name = "private-project",
            description = "A private project",
            visibility = ProjectVisibility.PRIVATE,
        )
        repository.initProjectResult = Result.success(CommandResult(success = true, message = "Created"))

        useCase(privateRequest)

        assertEquals(ProjectVisibility.PRIVATE, repository.lastInitRequest?.visibility)
    }
}
