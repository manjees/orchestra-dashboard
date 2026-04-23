package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import platform.Foundation.NSUserDefaults

class IOSNotificationLocalStore : NotificationLocalStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    private val version = MutableStateFlow(0)

    override suspend fun load(): NotificationSettings = readFromDefaults()

    override suspend fun save(settings: NotificationSettings) {
        defaults.setBool(settings.enabled, forKey = KEY_ENABLED)
        defaults.setBool(settings.notifyOnSuccess, forKey = KEY_NOTIFY_SUCCESS)
        defaults.setBool(settings.notifyOnFailure, forKey = KEY_NOTIFY_FAILURE)
        defaults.synchronize()
        version.value++
    }

    override fun observe(): Flow<NotificationSettings> = version.map { readFromDefaults() }

    private fun readFromDefaults(): NotificationSettings =
        NotificationSettings(
            // NSUserDefaults boolForKey returns false by default; the first launch should
            // treat absent values as enabled to match other platforms.
            enabled = boolOrDefault(KEY_ENABLED, default = true),
            notifyOnSuccess = boolOrDefault(KEY_NOTIFY_SUCCESS, default = true),
            notifyOnFailure = boolOrDefault(KEY_NOTIFY_FAILURE, default = true),
        )

    private fun boolOrDefault(
        key: String,
        default: Boolean,
    ): Boolean {
        return if (defaults.objectForKey(key) == null) default else defaults.boolForKey(key)
    }

    companion object {
        private const val KEY_ENABLED = "orchestra_notifications_enabled"
        private const val KEY_NOTIFY_SUCCESS = "orchestra_notify_on_success"
        private const val KEY_NOTIFY_FAILURE = "orchestra_notify_on_failure"
    }
}
