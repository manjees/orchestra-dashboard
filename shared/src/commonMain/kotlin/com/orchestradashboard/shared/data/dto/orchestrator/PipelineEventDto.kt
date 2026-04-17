package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PipelineEventDto(
    val type: String,
    @SerialName("pipeline_id") val pipelineId: String? = null,
    val step: String? = null,
    val status: String? = null,
    val mode: String? = null,
    @SerialName("elapsed_sec") val elapsedSec: Double? = null,
    val detail: String? = null,
    @SerialName("pr_url") val prUrl: String? = null,
    @SerialName("failed_step") val failedStep: String? = null,
    @SerialName("approval_id") val approvalId: String? = null,
    @SerialName("approval_type") val approvalType: String? = null,
    val ruling: String? = null,
    val options: List<String>? = null,
    val context: ApprovalContextDto? = null,
    @SerialName("timeout_sec") val timeoutSec: Int? = null,
    @SerialName("ram_percent") val ramPercent: Double? = null,
    @SerialName("cpu_percent") val cpuPercent: Double? = null,
    val thermal: String? = null,
    val timestamp: String? = null,
    @SerialName("lane_id") val laneId: String? = null,
)
