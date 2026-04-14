package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.DiscussResult
import com.orchestradashboard.shared.ui.commandcenter.FakeCommandRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiscussUseCaseTest {
    private val repository = FakeCommandRepository()
    private val useCase = DiscussUseCase(repository)

    @Test
    fun `invoke with project and question returns Result success with answer text`() =
        runTest {
            val expected = DiscussResult(answer = "Here is your answer", suggestedIssues = emptyList())
            repository.discussResult = Result.success(expected)

            val result = useCase("my-project", "What should I implement first?")

            assertTrue(result.isSuccess)
            assertEquals("Here is your answer", result.getOrNull()?.answer)
        }

    @Test
    fun `invoke when repository throws returns Result failure`() =
        runTest {
            repository.discussResult = Result.failure(RuntimeException("Discuss error"))

            val result = useCase("my-project", "question")

            assertTrue(result.isFailure)
            assertEquals("Discuss error", result.exceptionOrNull()?.message)
        }
}
