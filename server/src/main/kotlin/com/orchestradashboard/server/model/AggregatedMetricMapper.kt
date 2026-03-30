package com.orchestradashboard.server.model

import org.springframework.stereotype.Component

@Component
class AggregatedMetricMapper {
    fun toResponse(entity: AggregatedMetricEntity) =
        AggregatedMetricResponse(
            id = entity.id,
            agentId = entity.agentId,
            metricName = entity.metricName,
            avgValue = entity.avgValue,
            minValue = entity.minValue,
            maxValue = entity.maxValue,
            count = entity.count,
            timestampBucket = entity.timestampBucket,
            createdAt = entity.createdAt,
        )

    fun toResponseList(entities: List<AggregatedMetricEntity>) = entities.map(::toResponse)
}
