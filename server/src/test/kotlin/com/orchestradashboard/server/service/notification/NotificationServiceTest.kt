package com.orchestradashboard.server.service.notification

import com.orchestradashboard.server.model.notification.DeviceTokenRecord
import com.orchestradashboard.server.model.notification.DeviceTokenRequest
import com.orchestradashboard.server.model.notification.PipelineNotificationPayload
import com.orchestradashboard.server.repository.notification.DeviceTokenRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NotificationServiceTest {
    private val tokenRepository = DeviceTokenRepository()
    private val fcmSender: FcmSender = mock()
    private val apnsSender: ApnsSender = mock()
    private val service = NotificationService(tokenRepository, fcmSender, apnsSender)

    @Test
    fun `registerToken stores a new token and returns response`() {
        val response = service.registerToken(DeviceTokenRequest("tok-new", "android"))

        assertTrue(response.registeredAt > 0)
        assertEquals(1, tokenRepository.findAll().size)
        assertEquals("ANDROID", tokenRepository.findAll()[0].platform)
    }

    @Test
    fun `registerToken upserts when same token registered twice`() {
        service.registerToken(DeviceTokenRequest("tok-1", "ANDROID"))
        service.registerToken(DeviceTokenRequest("tok-1", "IOS"))

        val records = tokenRepository.findAll()
        assertEquals(1, records.size)
        assertEquals("IOS", records[0].platform)
    }

    @Test
    fun `unregisterToken returns true for existing token`() {
        tokenRepository.save(DeviceTokenRecord("tok-1", "ANDROID", 100L))

        val removed = service.unregisterToken("tok-1")

        assertTrue(removed)
        assertTrue(tokenRepository.findAll().isEmpty())
    }

    @Test
    fun `unregisterToken returns false when token missing`() {
        val removed = service.unregisterToken("missing")

        assertFalse(removed)
    }

    @Test
    fun `dispatch returns zeros when no tokens registered`() {
        val result =
            service.dispatch(
                PipelineNotificationPayload(
                    pipelineId = "pipe-1",
                    projectName = "orchestra",
                    status = "success",
                ),
            )

        assertEquals(0, result.attempted)
        assertEquals(0, result.succeeded)
        assertEquals(0, result.failed)
        verify(fcmSender, never()).send(any(), any())
        verify(apnsSender, never()).send(any(), any())
    }

    @Test
    fun `dispatch routes android tokens through FcmSender`() {
        tokenRepository.save(DeviceTokenRecord("tok-a", "ANDROID", 100L))
        tokenRepository.save(DeviceTokenRecord("tok-b", "ANDROID", 100L))
        whenever(fcmSender.send(any(), any())).thenReturn(true)

        val result =
            service.dispatch(
                PipelineNotificationPayload(
                    pipelineId = "pipe-1",
                    projectName = "orchestra",
                    status = "success",
                    issueNumber = 70,
                    prUrl = "https://github.com/acme/proj/pull/7",
                ),
            )

        assertEquals(2, result.attempted)
        assertEquals(2, result.succeeded)
        assertEquals(0, result.failed)
        verify(fcmSender).send(eq("tok-a"), any())
        verify(fcmSender).send(eq("tok-b"), any())
        verify(apnsSender, never()).send(any(), any())
    }

    @Test
    fun `dispatch routes mixed platforms and counts partial failures`() {
        tokenRepository.save(DeviceTokenRecord("tok-a", "ANDROID", 100L))
        tokenRepository.save(DeviceTokenRecord("tok-i", "IOS", 100L))
        tokenRepository.save(DeviceTokenRecord("tok-d", "DESKTOP", 100L))
        whenever(fcmSender.send(any(), any())).thenReturn(false)
        whenever(apnsSender.send(any(), any())).thenReturn(true)

        val result =
            service.dispatch(
                PipelineNotificationPayload(
                    pipelineId = "pipe-2",
                    projectName = "orchestra",
                    status = "failure",
                ),
            )

        assertEquals(3, result.attempted)
        // Android fails (1 fail), iOS succeeds (1), desktop is a no-op success (1)
        assertEquals(2, result.succeeded)
        assertEquals(1, result.failed)
        verify(fcmSender).send(eq("tok-a"), any())
        verify(apnsSender).send(eq("tok-i"), any())
    }
}
