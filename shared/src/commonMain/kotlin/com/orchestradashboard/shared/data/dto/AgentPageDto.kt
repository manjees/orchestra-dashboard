package com.orchestradashboard.shared.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AgentPageDto(
    val content: List<AgentDto>,
    val page: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
)
