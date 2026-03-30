package com.orchestradashboard.shared.domain.model

import kotlinx.datetime.Instant

data class Issue(
    val number: Int,
    val title: String,
    val labels: List<String>,
    val state: String,
    val createdAt: Instant,
)
