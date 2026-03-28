package com.orchestradashboard.server.model

data class PagedAgentResponse(
    val content: List<AgentResponse>,
    val page: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
)
