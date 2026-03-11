package com.orchestradashboard.shared.ui.util

import kotlinx.datetime.Clock

fun formatRelativeTime(epochMs: Long): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diffSeconds = (now - epochMs) / 1000
    return when {
        diffSeconds < 0 -> "just now"
        diffSeconds < 60 -> "${diffSeconds}s ago"
        diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
        diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
        else -> "${diffSeconds / 86400}d ago"
    }
}

/** Formats the total running duration since [createdAtEpochMs] as a compact duration string (e.g., "2h 15m"). */
fun formatUptime(createdAtEpochMs: Long): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diffSeconds = (now - createdAtEpochMs) / 1000
    if (diffSeconds < 0) return "0s"
    val days = diffSeconds / 86400
    val hours = (diffSeconds % 86400) / 3600
    val minutes = (diffSeconds % 3600) / 60
    val seconds = diffSeconds % 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
