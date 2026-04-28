package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.graphics.Color
import com.orchestradashboard.shared.domain.model.LogEntry
import com.orchestradashboard.shared.domain.model.LogLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LogStreamPanelStateTest {
    private fun entry(
        timestamp: String = "2026-04-23T10:15:30Z",
        level: LogLevel = LogLevel.INFO,
        message: String = "hello world",
        stepId: String = "step-1",
    ): LogEntry =
        LogEntry(
            timestamp = timestamp,
            level = level,
            message = message,
            stepId = stepId,
        )

    // ─── formatLogEntry ───────────────────────────────────────────────────────

    @Test
    fun `formatLogEntry formats INFO entry with HH-mm-ss prefix`() {
        val result = formatLogEntry(entry(timestamp = "2026-04-23T10:15:30Z", level = LogLevel.INFO, message = "ok"))
        assertEquals("[10:15:30] ok", result)
    }

    @Test
    fun `formatLogEntry formats ERROR entry with ERROR marker`() {
        val result =
            formatLogEntry(entry(timestamp = "2026-04-23T01:02:03Z", level = LogLevel.ERROR, message = "boom"))
        assertTrue(result.contains("ERROR"), "expected ERROR marker but was '$result'")
        assertTrue(result.contains("boom"))
        assertTrue(result.contains("01:02:03"))
    }

    @Test
    fun `formatLogEntry formats WARN entry with WARN marker`() {
        val result =
            formatLogEntry(entry(timestamp = "2026-04-23T23:59:59Z", level = LogLevel.WARN, message = "careful"))
        assertTrue(result.contains("WARN"), "expected WARN marker but was '$result'")
        assertTrue(result.contains("careful"))
        assertTrue(result.contains("23:59:59"))
    }

    @Test
    fun `formatLogEntry formats DEBUG entry with DEBUG marker`() {
        val result =
            formatLogEntry(entry(timestamp = "2026-04-23T00:00:00Z", level = LogLevel.DEBUG, message = "trace"))
        assertTrue(result.contains("DEBUG"), "expected DEBUG marker but was '$result'")
        assertTrue(result.contains("trace"))
    }

    @Test
    fun `formatLogEntry handles blank timestamp gracefully`() {
        val result = formatLogEntry(entry(timestamp = "", level = LogLevel.INFO, message = "hi"))
        assertNotNull(result)
        assertTrue(result.contains("hi"), "message should still be preserved but was '$result'")
    }

    @Test
    fun `formatLogEntry handles malformed timestamp gracefully`() {
        val result = formatLogEntry(entry(timestamp = "not-a-timestamp", level = LogLevel.INFO, message = "hi"))
        assertNotNull(result)
        assertTrue(result.contains("hi"))
    }

    // ─── logLevelColor ────────────────────────────────────────────────────────

    @Test
    fun `logLevelColor returns Unspecified for INFO`() {
        assertEquals(Color.Unspecified, logLevelColor(LogLevel.INFO))
    }

    @Test
    fun `logLevelColor returns a warning hued color for WARN`() {
        val color = logLevelColor(LogLevel.WARN)
        // Orange-ish: red and green high, blue low
        assertTrue(color.red > 0.6f, "expected high red channel but was ${color.red}")
        assertTrue(color.green > 0.3f, "expected moderate green channel but was ${color.green}")
        assertTrue(color.blue < 0.3f, "expected low blue channel but was ${color.blue}")
    }

    @Test
    fun `logLevelColor returns a red hued color for ERROR`() {
        val color = logLevelColor(LogLevel.ERROR)
        assertTrue(color.red > 0.7f, "expected high red channel but was ${color.red}")
        assertTrue(color.green < 0.4f, "expected low green channel but was ${color.green}")
        assertTrue(color.blue < 0.4f, "expected low blue channel but was ${color.blue}")
    }

    @Test
    fun `logLevelColor returns a gray hued color for DEBUG`() {
        val color = logLevelColor(LogLevel.DEBUG)
        // Gray: all channels roughly equal and moderate
        val max = maxOf(color.red, color.green, color.blue)
        val min = minOf(color.red, color.green, color.blue)
        assertTrue(max - min < 0.2f, "expected near-gray channels but was r=${color.red} g=${color.green} b=${color.blue}")
    }
}
