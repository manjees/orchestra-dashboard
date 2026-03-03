package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.MetricDto
import com.orchestradashboard.shared.domain.model.Metric

/**
 * Maps between [MetricDto] (network layer) and [Metric] (domain layer).
 */
class MetricMapper {

    /**
     * Converts an API metric DTO to a domain model.
     *
     * @param dto The raw metric DTO
     * @return Corresponding domain metric model
     */
    fun toDomain(dto: MetricDto): Metric {
        return Metric(
            agentId = dto.agentId,
            name = dto.name,
            value = dto.value,
            unit = dto.unit,
            timestamp = dto.timestamp
        )
    }

    /**
     * Converts a list of metric DTOs to domain models.
     *
     * @param dtos List of raw metric DTOs
     * @return List of domain metric models
     */
    fun toDomain(dtos: List<MetricDto>): List<Metric> = dtos.map(::toDomain)
}
