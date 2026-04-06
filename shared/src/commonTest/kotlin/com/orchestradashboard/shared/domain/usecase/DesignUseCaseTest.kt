package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.DesignResult
import com.orchestradashboard.shared.ui.commandcenter.FakeCommandRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesignUseCaseTest {
    private val repository = FakeCommandRepository()
    private val useCase = DesignUseCase(repository)

    @Test
    fun `invoke with project and figmaUrl returns Result success with UI spec`() = runTest {
        val expected = DesignResult(spec = "Component layout spec")
        repository.designResult = Result.success(expected)

        val result = useCase("my-project", "https://figma.com/file/abc")

        assertTrue(result.isSuccess)
        assertEquals("Component layout spec", result.getOrNull()?.spec)
    }

    @Test
    fun `invoke when repository throws returns Result failure`() = runTest {
        repository.designResult = Result.failure(RuntimeException("Design error"))

        val result = useCase("my-project", "https://figma.com/file/abc")

        assertTrue(result.isFailure)
        assertEquals("Design error", result.exceptionOrNull()?.message)
    }
}
