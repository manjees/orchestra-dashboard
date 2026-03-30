package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.TimeRange
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TimeRangeSelectorTest {
    @Test
    fun `should display all time range options`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    TimeRangeSelector(
                        selected = TimeRange.Last24Hours,
                        onSelected = {},
                    )
                }
            }
            onNodeWithText("24h").assertIsDisplayed()
            onNodeWithText("7d").assertIsDisplayed()
            onNodeWithText("30d").assertIsDisplayed()
        }

    @Test
    fun `should invoke callback when time range clicked`() =
        runComposeUiTest {
            var selected: TimeRange? = null
            setContent {
                DashboardTheme {
                    TimeRangeSelector(
                        selected = TimeRange.Last24Hours,
                        onSelected = { selected = it },
                    )
                }
            }
            onNodeWithText("7d").performClick()
            assertEquals(TimeRange.Last7Days, selected)
        }
}
