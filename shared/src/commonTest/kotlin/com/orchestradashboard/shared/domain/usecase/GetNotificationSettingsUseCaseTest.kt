package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.ui.settings.FakeNotificationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetNotificationSettingsUseCaseTest {
    private val repository = FakeNotificationRepository()
    private val useCase = GetNotificationSettingsUseCase(repository)

    @Test
    fun `invoke returns settings stored in repository`() =
        runTest {
            repository.currentSettings =
                NotificationSettings(
                    enabled = false,
                    notifyOnSuccess = true,
                    notifyOnFailure = false,
                )

            val result = useCase()

            assertEquals(false, result.enabled)
            assertEquals(true, result.notifyOnSuccess)
            assertEquals(false, result.notifyOnFailure)
        }
}
