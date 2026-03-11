package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ErrorBannerTest {
    @Test
    fun `should display error message`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ErrorBanner(
                        message = "Connection failed",
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("Connection failed").assertIsDisplayed()
        }

    @Test
    fun `should display dismiss button`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ErrorBanner(
                        message = "Error occurred",
                        onDismiss = {},
                    )
                }
            }
            onNodeWithContentDescription("Dismiss").assertIsDisplayed()
        }

    @Test
    fun `should invoke onDismiss when dismiss button is clicked`() =
        runComposeUiTest {
            var dismissed = false
            setContent {
                DashboardTheme {
                    ErrorBanner(
                        message = "Error occurred",
                        onDismiss = { dismissed = true },
                    )
                }
            }
            onNodeWithContentDescription("Dismiss").performClick()
            assertTrue(dismissed)
        }
}
