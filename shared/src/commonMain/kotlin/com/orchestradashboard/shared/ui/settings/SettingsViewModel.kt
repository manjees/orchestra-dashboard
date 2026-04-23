package com.orchestradashboard.shared.ui.settings

import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.domain.usecase.GetNotificationSettingsUseCase
import com.orchestradashboard.shared.domain.usecase.GetSettingsUseCase
import com.orchestradashboard.shared.domain.usecase.SaveNotificationSettingsUseCase
import com.orchestradashboard.shared.domain.usecase.SaveSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val saveNotificationSettingsUseCase: SaveNotificationSettingsUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadSettings() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val settings = getSettingsUseCase()
                val notif = getNotificationSettingsUseCase()
                _uiState.update {
                    it.copy(
                        baseUrl = settings.baseUrl,
                        apiKey = settings.apiKey,
                        notificationsEnabled = notif.enabled,
                        notifyOnSuccess = notif.notifyOnSuccess,
                        notifyOnFailure = notif.notifyOnFailure,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load settings",
                    )
                }
            }
        }
    }

    fun updateBaseUrl(url: String) {
        _uiState.update { it.copy(baseUrl = url, saveSuccess = false) }
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key, saveSuccess = false) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled, saveSuccess = false) }
        persistNotificationSettings()
    }

    fun toggleNotifyOnSuccess(enabled: Boolean) {
        _uiState.update { it.copy(notifyOnSuccess = enabled, saveSuccess = false) }
        persistNotificationSettings()
    }

    fun toggleNotifyOnFailure(enabled: Boolean) {
        _uiState.update { it.copy(notifyOnFailure = enabled, saveSuccess = false) }
        persistNotificationSettings()
    }

    fun saveSettings() {
        val state = _uiState.value
        if (state.baseUrl.isBlank()) {
            _uiState.update { it.copy(error = "Base URL cannot be empty") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
        viewModelScope.launch {
            try {
                saveSettingsUseCase(
                    baseUrl = state.baseUrl.trim(),
                    apiKey = state.apiKey.trim(),
                )
                saveNotificationSettingsUseCase(
                    NotificationSettings(
                        enabled = state.notificationsEnabled,
                        notifyOnSuccess = state.notifyOnSuccess,
                        notifyOnFailure = state.notifyOnFailure,
                    ),
                )
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save settings",
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }

    private fun persistNotificationSettings() {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                saveNotificationSettingsUseCase(
                    NotificationSettings(
                        enabled = state.notificationsEnabled,
                        notifyOnSuccess = state.notifyOnSuccess,
                        notifyOnFailure = state.notifyOnFailure,
                    ),
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to save notifications") }
            }
        }
    }
}
