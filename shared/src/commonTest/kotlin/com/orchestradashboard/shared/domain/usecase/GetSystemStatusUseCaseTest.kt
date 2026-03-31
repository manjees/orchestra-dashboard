package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.SystemStatus
import com.orchestradashboard.shared.domain.model.ThermalPressure
import com.orchestradashboard.shared.ui.dashboardhome.FakeSystemRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetSystemStatusUseCaseTest {
    private val repository = FakeSystemRepository()
    private val useCase = GetSystemStatusUseCase(repository)

    @Test
    fun `invoke delegates to repository getSystemStatus`() =
        runTest {
            val expected = SystemStatus(80.0, 60.0, 70.0, ThermalPressure.MODERATE)
            repository.systemStatusResult = Result.success(expected)

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(expected, result.getOrThrow())
            assertEquals(1, repository.getSystemStatusCallCount)
        }

    @Test
    fun `invoke returns failure on repository error`() =
        runTest {
            repository.systemStatusResult = Result.failure(RuntimeException("fail"))

            val result = useCase()

            assertTrue(result.isFailure)
        }
}
