package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InitProjectRequestDto(
    val name: String,
    val description: String,
    val visibility: String,
)

@Serializable
data class InitProjectResponseDto(
    val success: Boolean,
    val message: String,
    @SerialName("pipeline_id") val pipelineId: String? = null,
)

@Serializable
data class PlanIssuesRequestDto(
    val project: String,
)

@Serializable
data class PlannedIssueDto(
    val title: String,
    val body: String,
    val labels: List<String>,
)

@Serializable
data class PlanIssuesResponseDto(
    val issues: List<PlannedIssueDto>,
)

@Serializable
data class DiscussRequestDto(
    val project: String,
    val question: String,
)

@Serializable
data class DiscussResponseDto(
    val answer: String,
    @SerialName("suggested_issues") val suggestedIssues: List<PlannedIssueDto> = emptyList(),
)

@Serializable
data class DesignRequestDto(
    val project: String,
    @SerialName("figma_url") val figmaUrl: String,
)

@Serializable
data class DesignResponseDto(
    val spec: String,
    @SerialName("suggested_issues") val suggestedIssues: List<PlannedIssueDto> = emptyList(),
)

@Serializable
data class ShellRequestDto(
    val command: String,
)

@Serializable
data class ShellResponseDto(
    val output: String,
    @SerialName("exit_code") val exitCode: Int,
)
