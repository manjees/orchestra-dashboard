package com.orchestradashboard.shared.domain.model

data class ActivePipeline(
    val id: String,
    val projectName: String,
    val issueNum: Int,
    val issueTitle: String,
    val currentStep: String,
    val elapsedTotalSec: Double,
    val status: String,
)
