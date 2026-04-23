package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.NotificationMapper
import com.orchestradashboard.shared.data.network.FakeDashboardApiClient
import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.domain.model.NotificationStatus
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import com.orchestradashboard.shared.push.FakeNotificationLocalStore
import com.orchestradashboard.shared.push.FakePushNotificationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationRepositoryImplTest {
    private val api = FakeDashboardApiClient()
    private val mapper = NotificationMapper()
    private val localStore = FakeNotificationLocalStore()
    private val pushProvider = FakePushNotificationProvider()
    private val repository = NotificationRepositoryImpl(api, mapper, localStore, pushProvider)

    @Test
    fun `registerDeviceToken forwards token to api and returns success`() =
        runTest {
            val result = repository.registerDeviceToken("tok-1", DevicePlatform.ANDROID)

            assertTrue(result.isSuccess)
            assertEquals("tok-1", api.lastRegisteredDeviceToken?.token)
            assertEquals("ANDROID", api.lastRegisteredDeviceToken?.platform)
        }

    @Test
    fun `registerDeviceToken returns failure on api exception`() =
        runTest {
            api.errorToThrow = RuntimeException("boom")

            val result = repository.registerDeviceToken("tok-2", DevicePlatform.IOS)

            assertTrue(result.isFailure)
            assertEquals("boom", result.exceptionOrNull()?.message)
        }

    @Test
    fun `unregisterDeviceToken calls api and returns success`() =
        runTest {
            val result = repository.unregisterDeviceToken("tok-3")

            assertTrue(result.isSuccess)
            assertEquals("tok-3", api.lastUnregisteredDeviceToken)
        }

    @Test
    fun `unregisterDeviceToken returns failure when api throws`() =
        runTest {
            api.errorToThrow = RuntimeException("no network")

            val result = repository.unregisterDeviceToken("tok-4")

            assertTrue(result.isFailure)
        }

    @Test
    fun `getNotificationSettings delegates to local store`() =
        runTest {
            localStore.save(NotificationSettings(enabled = false, notifyOnSuccess = false))

            val result = repository.getNotificationSettings()

            assertEquals(false, result.enabled)
            assertEquals(false, result.notifyOnSuccess)
        }

    @Test
    fun `saveNotificationSettings writes to local store`() =
        runTest {
            repository.saveNotificationSettings(NotificationSettings(enabled = false))

            assertEquals(1, localStore.saveCallCount)
            assertEquals(false, repository.getNotificationSettings().enabled)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeIncomingNotifications mirrors provider flow`() =
        runTest(UnconfinedTestDispatcher()) {
            val payload =
                PushNotificationPayload(
                    projectName = "orchestra",
                    pipelineId = "pipe-42",
                    status = NotificationStatus.FAILURE,
                    timestamp = 999L,
                )

            val deferred = async { repository.observeIncomingNotifications().first() }
            pushProvider.emitIncoming(payload)
            val received = deferred.await()

            assertEquals(payload, received)
        }
}
