package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogEntryDto(
    val timestamp: String? = null,
    val level: String? = null,
    val message: String = "",
    @SerialName("step_id") val stepId: String? = null,
)
