package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.ui.settings.FakeNotificationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveNotificationSettingsUseCaseTest {
    private val repository = FakeNotificationRepository()
    private val useCase = SaveNotificationSettingsUseCase(repository)

    @Test
    fun `invoke delegates settings to repository`() =
        runTest {
            val input =
                NotificationSettings(
                    enabled = true,
                    notifyOnSuccess = false,
                    notifyOnFailure = true,
                )

            useCase(input)

            assertEquals(input, repository.currentSettings)
            assertEquals(1, repository.saveCallCount)
        }

    @Test
    fun `invoke called multiple times persists latest settings`() =
        runTest {
            useCase(NotificationSettings(enabled = true))
            useCase(NotificationSettings(enabled = false))

            assertEquals(false, repository.currentSettings.enabled)
            assertEquals(2, repository.saveCallCount)
        }
}
