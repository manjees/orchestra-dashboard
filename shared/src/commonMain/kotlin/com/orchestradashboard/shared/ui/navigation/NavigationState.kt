package com.orchestradashboard.shared.ui.navigation

sealed class NavigationState {
    data object Dashboard : NavigationState()

    data class AgentDetail(val agentId: String) : NavigationState() {
        init {
            require(agentId.isNotBlank()) { "agentId must not be blank" }
        }
    }
}
