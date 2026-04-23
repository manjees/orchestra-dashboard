package com.orchestradashboard.shared.domain.model

data class HistoryStep(
    val stepName: String,
    val status: StepStatus,
    val elapsedSec: Double,
    val failDetail: String?,
)
