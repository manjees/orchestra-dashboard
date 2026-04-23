package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.Serializable

/**
 * DTO for the Spring Boot `Page<PipelineHistoryResponse>` wrapper.
 * Spring serializes `Page<T>` with camelCase Jackson defaults — the wrapper fields are:
 * `content`, `number`, `size`, `totalElements`, `totalPages`, `first`, `last`.
 */
@Serializable
data class PipelineHistoryPageDto(
    val content: List<PipelineHistoryDetailDto> = emptyList(),
    val number: Int = 0,
    val size: Int = 20,
    val totalElements: Long = 0L,
    val totalPages: Int = 0,
)
