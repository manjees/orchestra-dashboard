package com.orchestradashboard.shared.push

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.orchestradashboard.shared.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class AndroidNotificationLocalStore(context: Context) : NotificationLocalStore {
    private val prefs: SharedPreferences by lazy {
        val masterKey =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val version = MutableStateFlow(0)

    override suspend fun load(): NotificationSettings = readFromPrefs()

    override suspend fun save(settings: NotificationSettings) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, settings.enabled)
            .putBoolean(KEY_NOTIFY_SUCCESS, settings.notifyOnSuccess)
            .putBoolean(KEY_NOTIFY_FAILURE, settings.notifyOnFailure)
            .apply()
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
        private const val PREFS_FILE_NAME = "orchestra_notification_prefs"
        private const val KEY_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFY_SUCCESS = "notify_on_success"
        private const val KEY_NOTIFY_FAILURE = "notify_on_failure"
    }
}
