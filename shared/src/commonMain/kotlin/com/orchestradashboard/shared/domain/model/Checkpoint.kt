package com.orchestradashboard.shared.domain.model

import kotlinx.datetime.Instant

data class Checkpoint(
    val id: String,
    val pipelineId: String,
    val createdAt: Instant,
    val step: String,
    val status: CheckpointStatus,
)

enum class CheckpointStatus { PASSED, FAILED, PENDING, RUNNING, UNKNOWN }
