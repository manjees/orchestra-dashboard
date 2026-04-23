package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.prefs.Preferences

class DesktopNotificationLocalStore(
    private val prefs: Preferences = Preferences.userNodeForPackage(DesktopNotificationLocalStore::class.java),
) : NotificationLocalStore {
    private val version = MutableStateFlow(0)

    override suspend fun load(): NotificationSettings = readFromPrefs()

    override suspend fun save(settings: NotificationSettings) {
        prefs.putBoolean(KEY_ENABLED, settings.enabled)
        prefs.putBoolean(KEY_NOTIFY_SUCCESS, settings.notifyOnSuccess)
        prefs.putBoolean(KEY_NOTIFY_FAILURE, settings.notifyOnFailure)
        prefs.flush()
        version.value++
    }

    override fun observe(): Flow<NotificationSettings> = version.map { readFromPrefs() }

    private fun readFromPrefs(): NotificationSettings =
        NotificationSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, true),
            notifyOnSuccess = prefs.getBoolean(KEY_NOTIFY_SUCCESS, true),
            notifyOnFailure = prefs.getBoolean(KEY_NOTIFY_FAILURE, true),
        )

    companion object {
        private const val KEY_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFY_SUCCESS = "notify_on_success"
        private const val KEY_NOTIFY_FAILURE = "notify_on_failure"
    }
}
