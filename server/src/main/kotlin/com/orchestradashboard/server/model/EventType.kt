package com.orchestradashboard.server.model

enum class EventType {
    HEARTBEAT,
    STATUS_CHANGE,
    PIPELINE_STARTED,
    PIPELINE_COMPLETED,
    ERROR,
    ;

    companion object {
        fun isValid(value: String): Boolean = entries.any { it.name == value }
    }
}
