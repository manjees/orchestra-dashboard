package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getBaseUrl(): String

    suspend fun saveBaseUrl(url: String)

    suspend fun getApiKey(): String

    suspend fun saveApiKey(key: String)

    fun observeSettings(): Flow<AppSettings>
}
