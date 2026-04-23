package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.NotificationStatus
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import com.orchestradashboard.shared.ui.settings.FakeNotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ObserveIncomingNotificationsUseCaseTest {
    private val repository = FakeNotificationRepository()
    private val useCase = ObserveIncomingNotificationsUseCase(repository)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke emits payloads from repository`() =
        runTest(UnconfinedTestDispatcher()) {
            val payload =
                PushNotificationPayload(
                    projectName = "orchestra",
                    pipelineId = "pipe-1",
                    status = NotificationStatus.SUCCESS,
                    timestamp = 123L,
                )
            val flow = useCase()

            // Collect async; emit afterwards. Use replay via a deferred capture.
            val deferred = async { flow.first() }
            repository.emitIncoming(payload)
            val received = deferred.await()

            assertNotNull(received)
            assertEquals(payload, received)
        }
}
