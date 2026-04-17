package com.orchestradashboard.shared.data.dto.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StepFailureDto(
    @SerialName("step_name") val stepName: String,
    @SerialName("total_count") val totalCount: Int,
    @SerialName("failed_count") val failedCount: Int,
    @SerialName("failure_rate") val failureRate: Double,
)
