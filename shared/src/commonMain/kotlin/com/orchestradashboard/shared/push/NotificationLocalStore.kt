package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific local storage for notification preferences.
 *
 * - Android: EncryptedSharedPreferences
 * - Desktop: java.util.prefs.Preferences
 * - iOS: NSUserDefaults
 */
interface NotificationLocalStore {
    suspend fun load(): NotificationSettings

    suspend fun save(settings: NotificationSettings)

    fun observe(): Flow<NotificationSettings>
}
