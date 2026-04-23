package com.orchestradashboard.server.repository.notification

import com.orchestradashboard.server.model.notification.DeviceTokenRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DeviceTokenRepositoryTest {
    private val repository = DeviceTokenRepository()

    @Test
    fun `save stores the record and findAll returns it`() {
        val record = DeviceTokenRecord("tok-1", "ANDROID", 100L)

        repository.save(record)

        val stored = repository.findAll()
        assertEquals(1, stored.size)
        assertEquals("tok-1", stored[0].token)
    }

    @Test
    fun `save overwrites previous record with same token`() {
        repository.save(DeviceTokenRecord("tok-1", "ANDROID", 100L))
        repository.save(DeviceTokenRecord("tok-1", "IOS", 200L))

        val records = repository.findAll()
        assertEquals(1, records.size)
        assertEquals("IOS", records[0].platform)
        assertEquals(200L, records[0].createdAt)
    }

    @Test
    fun `remove returns true when token existed`() {
        repository.save(DeviceTokenRecord("tok-1", "ANDROID", 100L))

        val removed = repository.remove("tok-1")

        assertTrue(removed)
        assertTrue(repository.findAll().isEmpty())
    }

    @Test
    fun `remove returns false when token did not exist`() {
        val removed = repository.remove("missing")

        assertFalse(removed)
    }

    @Test
    fun `findByPlatform filters tokens case-insensitively`() {
        repository.save(DeviceTokenRecord("tok-1", "ANDROID", 100L))
        repository.save(DeviceTokenRecord("tok-2", "android", 150L))
        repository.save(DeviceTokenRecord("tok-3", "IOS", 200L))

        val android = repository.findByPlatform("ANDROID")

        assertEquals(2, android.size)
        assertTrue(android.all { it.platform.equals("android", ignoreCase = true) })
    }
}
