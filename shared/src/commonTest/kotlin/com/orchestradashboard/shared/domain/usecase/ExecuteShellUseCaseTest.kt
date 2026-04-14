package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.ShellResult
import com.orchestradashboard.shared.ui.commandcenter.FakeCommandRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExecuteShellUseCaseTest {
    private val repository = FakeCommandRepository()
    private val useCase = ExecuteShellUseCase(repository)

    @Test
    fun `invoke with safe command returns Result success with output`() =
        runTest {
            val expected = ShellResult(output = "file1.txt\nfile2.txt", exitCode = 0)
            repository.shellResult = Result.success(expected)

            val result = useCase("ls -la")

            assertTrue(result.isSuccess)
            assertEquals("file1.txt\nfile2.txt", result.getOrNull()?.output)
            assertEquals(0, result.getOrNull()?.exitCode)
        }

    @Test
    fun `invoke when repository throws returns Result failure`() =
        runTest {
            repository.shellResult = Result.failure(RuntimeException("Shell error"))

            val result = useCase("ls")

            assertTrue(result.isFailure)
            assertEquals("Shell error", result.exceptionOrNull()?.message)
        }
}
