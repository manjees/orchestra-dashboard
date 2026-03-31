package com.orchestradashboard.shared.domain.model

data class Project(
    val name: String,
    val path: String,
    val ciCommands: List<String>,
    val openIssuesCount: Int,
    val recentSolves: Int,
)
