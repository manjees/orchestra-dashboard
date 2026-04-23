package com.orchestradashboard.shared.domain.model

enum class PeriodFilter {
    WEEK,
    MONTH,
    ALL,
    ;

    val label: String
        get() =
            when (this) {
                WEEK -> "Week"
                MONTH -> "Month"
                ALL -> "All"
            }
}
