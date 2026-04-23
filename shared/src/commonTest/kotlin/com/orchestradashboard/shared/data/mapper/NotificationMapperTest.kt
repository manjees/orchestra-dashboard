package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.DeviceToken
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationMapperTest {
    private val mapper = NotificationMapper()

    @Test
    fun `toDto from DeviceToken maps token and platform name`() {
        val token = DeviceToken(token = "abc123", platform = DevicePlatform.ANDROID, createdAt = 100L)

        val dto = mapper.toDto(token)

        assertEquals("abc123", dto.token)
        assertEquals("ANDROID", dto.platform)
    }

    @Test
    fun `toDto from raw token and platform maps correctly`() {
        val dto = mapper.toDto("ios-token", DevicePlatform.IOS)

        assertEquals("ios-token", dto.token)
        assertEquals("IOS", dto.platform)
    }

    @Test
    fun `toDto desktop platform uses uppercased DESKTOP name`() {
        val dto = mapper.toDto("desktop-token", DevicePlatform.DESKTOP)

        assertEquals("DESKTOP", dto.platform)
    }
}
