package com.orchestradashboard.shared.ui.settings

data class SettingsUiState(
    val baseUrl: String = "",
    val apiKey: String = "",
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
)
