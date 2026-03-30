package com.orchestradashboard.shared.domain.model

enum class TimeRange(val label: String, val hours: Int) {
    Last24Hours("24h", 24),
    Last7Days("7d", 168),
    Last30Days("30d", 720),
}
