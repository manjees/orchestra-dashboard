package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PlannedIssue
import com.orchestradashboard.shared.domain.model.PlanIssuesResult
import com.orchestradashboard.shared.ui.commandcenter.FakeCommandRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlanIssuesUseCaseTest {
    private val repository = FakeCommandRepository()
    private val useCase = PlanIssuesUseCase(repository)

    @Test
    fun `invoke with project name returns Result success with list of planned issues`() = runTest {
        val issues = listOf(
            PlannedIssue(title = "Add auth", body = "Implement JWT", labels = listOf("feature")),
        )
        repository.planIssuesResult = Result.success(PlanIssuesResult(issues))

        val result = useCase("my-project")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.issues?.size)
        assertEquals("Add auth", result.getOrNull()?.issues?.first()?.title)
    }

    @Test
    fun `invoke when repository throws returns Result failure`() = runTest {
        repository.planIssuesResult = Result.failure(RuntimeException("Plan error"))

        val result = useCase("my-project")

        assertTrue(result.isFailure)
        assertEquals("Plan error", result.exceptionOrNull()?.message)
    }
}
