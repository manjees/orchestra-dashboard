package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.domain.model.AppSettings
import com.orchestradashboard.shared.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.prefs.Preferences

class DesktopSettingsRepository : SettingsRepository {
    private val prefs: Preferences =
        Preferences.userNodeForPackage(DesktopSettingsRepository::class.java)

    private val settingsVersion = MutableStateFlow(0)

    override suspend fun getBaseUrl(): String = prefs.get(KEY_BASE_URL, DEFAULT_BASE_URL)

    override suspend fun saveBaseUrl(url: String) {
        prefs.put(KEY_BASE_URL, url)
        prefs.flush()
        settingsVersion.value++
    }

    override suspend fun getApiKey(): String = prefs.get(KEY_API_KEY, "")

    override suspend fun saveApiKey(key: String) {
        prefs.put(KEY_API_KEY, key)
        prefs.flush()
        settingsVersion.value++
    }

    override fun observeSettings(): Flow<AppSettings> =
        settingsVersion.map {
            AppSettings(
                baseUrl = prefs.get(KEY_BASE_URL, DEFAULT_BASE_URL),
                apiKey = prefs.get(KEY_API_KEY, ""),
            )
        }

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_KEY = "api_key"
        private const val DEFAULT_BASE_URL = "http://localhost:9000"
    }
}
