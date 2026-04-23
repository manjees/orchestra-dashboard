package com.orchestradashboard.server.repository.notification

import com.orchestradashboard.server.model.notification.DeviceTokenRecord
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory device token store.
 *
 * NOTE: Tokens are lost on server restart. A durable backing store
 * (Redis / Postgres) should replace this in a follow-up issue once the push
 * contract is stabilised.
 */
@Repository
class DeviceTokenRepository {
    private val tokens = ConcurrentHashMap<String, DeviceTokenRecord>()

    fun save(record: DeviceTokenRecord): DeviceTokenRecord {
        tokens[record.token] = record
        return record
    }

    fun remove(token: String): Boolean = tokens.remove(token) != null

    fun findAll(): List<DeviceTokenRecord> = tokens.values.toList()

    fun findByPlatform(platform: String): List<DeviceTokenRecord> =
        tokens.values.filter {
            it.platform.equals(platform, ignoreCase = true)
        }
}
