package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.ui.settings.FakeNotificationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterDeviceTokenUseCaseTest {
    private val repository = FakeNotificationRepository()
    private val useCase = RegisterDeviceTokenUseCase(repository)

    @Test
    fun `invoke delegates token and platform and returns Result success`() =
        runTest {
            val result = useCase("fcm-token-1", DevicePlatform.ANDROID)

            assertTrue(result.isSuccess)
            assertEquals("fcm-token-1", repository.lastRegisteredToken)
            assertEquals(DevicePlatform.ANDROID, repository.lastRegisteredPlatform)
        }

    @Test
    fun `invoke returns Result failure when repository fails`() =
        runTest {
            repository.registerResult = Result.failure(RuntimeException("network down"))

            val result = useCase("fcm-token-2", DevicePlatform.IOS)

            assertTrue(result.isFailure)
            assertEquals("network down", result.exceptionOrNull()?.message)
        }
}
