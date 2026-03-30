package com.orchestradashboard.shared.domain.model

data class TimeSeriesData(
    val agentId: String,
    val metricName: String,
    val dataPoints: List<DataPoint>,
    val average: Double?,
    val min: Double?,
    val max: Double?,
    val sampleCount: Int,
    val fromTimestamp: Long,
    val toTimestamp: Long,
) {
    data class DataPoint(
        val timestamp: Long,
        val value: Double,
    )
}
