package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.TimeSeriesData
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class MetricsChartTest {
    @Test
    fun `should render chart component with valid time series data`() =
        runComposeUiTest {
            val data =
                TimeSeriesData(
                    agentId = "agent-1",
                    metricName = "cpu_usage",
                    dataPoints =
                        listOf(
                            TimeSeriesData.DataPoint(timestamp = 1000L, value = 40.0),
                            TimeSeriesData.DataPoint(timestamp = 2000L, value = 60.0),
                            TimeSeriesData.DataPoint(timestamp = 3000L, value = 80.0),
                        ),
                    average = 60.0,
                    min = 40.0,
                    max = 80.0,
                    sampleCount = 3,
                    fromTimestamp = 1000L,
                    toTimestamp = 3000L,
                )
            setContent {
                DashboardTheme {
                    MetricsChart(timeSeriesData = data)
                }
            }
            onNodeWithText("cpu_usage").assertIsDisplayed()
            onNodeWithText("Avg: 60.0").assertIsDisplayed()
        }

    @Test
    fun `should render empty state when no data points`() =
        runComposeUiTest {
            val data =
                TimeSeriesData(
                    agentId = "agent-1",
                    metricName = "cpu_usage",
                    dataPoints = emptyList(),
                    average = null,
                    min = null,
                    max = null,
                    sampleCount = 0,
                    fromTimestamp = 1000L,
                    toTimestamp = 3000L,
                )
            setContent {
                DashboardTheme {
                    MetricsChart(timeSeriesData = data)
                }
            }
            onNodeWithText("cpu_usage").assertIsDisplayed()
            onNodeWithText("No data available").assertIsDisplayed()
        }
}
