package com.orchestradashboard.shared.domain.model

data class StepFailureRate(
    val stepName: String,
    val totalCount: Int,
    val failedCount: Int,
    val failureRate: Double,
)
