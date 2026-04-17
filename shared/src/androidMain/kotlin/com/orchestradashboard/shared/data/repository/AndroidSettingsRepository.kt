package com.orchestradashboard.shared.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.orchestradashboard.shared.domain.model.AppSettings
import com.orchestradashboard.shared.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class AndroidSettingsRepository(context: Context) : SettingsRepository {
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

    private val settingsVersion = MutableStateFlow(0)

    override suspend fun getBaseUrl(): String = prefs.getString(KEY_BASE_URL, null) ?: DEFAULT_BASE_URL

    override suspend fun saveBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url).apply()
        settingsVersion.value++
    }

    override suspend fun getApiKey(): String = prefs.getString(KEY_API_KEY, null) ?: ""

    override suspend fun saveApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key).apply()
        settingsVersion.value++
    }

    override fun observeSettings(): Flow<AppSettings> =
        settingsVersion.map {
            AppSettings(
                baseUrl = prefs.getString(KEY_BASE_URL, null) ?: DEFAULT_BASE_URL,
                apiKey = prefs.getString(KEY_API_KEY, null) ?: "",
            )
        }

    companion object {
        private const val PREFS_FILE_NAME = "orchestra_settings_prefs"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_KEY = "api_key"
        private const val DEFAULT_BASE_URL = "http://localhost:9000"
    }
}
