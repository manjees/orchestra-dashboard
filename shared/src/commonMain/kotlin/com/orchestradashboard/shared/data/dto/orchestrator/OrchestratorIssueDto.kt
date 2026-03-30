package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrchestratorIssueDto(
    val number: Int,
    val title: String,
    val labels: List<String>,
    val state: String,
    @SerialName("created_at") val createdAt: String,
)
