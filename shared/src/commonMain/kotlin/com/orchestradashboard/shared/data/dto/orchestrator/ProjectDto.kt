package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val name: String,
    val path: String,
    @SerialName("ci_commands") val ciCommands: List<String>,
    @SerialName("open_issues_count") val openIssuesCount: Int,
    @SerialName("recent_solves") val recentSolves: Int,
)

@Serializable
data class ProjectDetailDto(
    val name: String,
    val path: String,
    @SerialName("ci_commands") val ciCommands: List<String>,
    @SerialName("open_issues_count") val openIssuesCount: Int,
    @SerialName("recent_solves") val recentSolves: Int,
    val summary: String? = null,
)
