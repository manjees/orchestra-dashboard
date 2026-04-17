package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.domain.model.AppSettings
import com.orchestradashboard.shared.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import platform.Foundation.NSUserDefaults

class IOSSettingsRepository : SettingsRepository {
    private val defaults = NSUserDefaults.standardUserDefaults

    private val settingsVersion = MutableStateFlow(0)

    override suspend fun getBaseUrl(): String = defaults.stringForKey(KEY_BASE_URL) ?: DEFAULT_BASE_URL

    override suspend fun saveBaseUrl(url: String) {
        defaults.setObject(url, forKey = KEY_BASE_URL)
        defaults.synchronize()
        settingsVersion.value++
    }

    // TODO: Phase 2 — Migrate API key storage to iOS Keychain for production security.
    //  Currently using NSUserDefaults for simplicity during initial implementation.
    override suspend fun getApiKey(): String = defaults.stringForKey(KEY_API_KEY) ?: ""

    override suspend fun saveApiKey(key: String) {
        defaults.setObject(key, forKey = KEY_API_KEY)
        defaults.synchronize()
        settingsVersion.value++
    }

    override fun observeSettings(): Flow<AppSettings> =
        settingsVersion.map {
            AppSettings(
                baseUrl = defaults.stringForKey(KEY_BASE_URL) ?: DEFAULT_BASE_URL,
                apiKey = defaults.stringForKey(KEY_API_KEY) ?: "",
            )
        }

    companion object {
        private const val KEY_BASE_URL = "orchestra_base_url"
        private const val KEY_API_KEY = "orchestra_api_key"
        private const val DEFAULT_BASE_URL = "http://localhost:9000"
    }
}
