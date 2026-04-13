package com.orchestradashboard.shared.domain.model

enum class SolveMode { EXPRESS, STANDARD, FULL, AUTO }

data class SolveRequest(
    val projectName: String,
    val issueNumbers: List<Int>,
    val mode: SolveMode,
    val parallel: Boolean,
)

data class SolveResponse(
    val pipelineId: String,
    val status: String,
)
