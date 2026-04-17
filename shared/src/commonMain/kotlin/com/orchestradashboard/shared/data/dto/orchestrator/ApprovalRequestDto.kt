package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.Serializable

@Serializable
data class ApprovalRequestDto(
    val decision: String,
    val comment: String = "",
)
