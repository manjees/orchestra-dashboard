package com.orchestradashboard.shared.domain.model

data class PagedResult<T>(
    val agents: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    val hasNextPage: Boolean get() = page < totalPages - 1
    val hasPreviousPage: Boolean get() = page > 0
}
