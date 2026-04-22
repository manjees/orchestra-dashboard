package com.orchestradashboard.shared.ui.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.orchestradashboard.shared.domain.model.ApprovalContext
import com.orchestradashboard.shared.domain.model.ApprovalRequest

@Preview
@Composable
fun ApprovalDialogStrategyModePreview() {
    val request =
        ApprovalRequest(
            id = "req-1",
            approvalType = "strategy",
            options = listOf("split_execute", "no_split", "cancel"),
            context = ApprovalContext(eta = "5m", detail = "Deploy to production"),
            timeoutSec = 300,
        )
    ApprovalDialog(
        approval = request,
        remainingTimeSec = 250,
        isTimedOut = false,
        isSubmitting = false,
        error = null,
        onRespond = {},
        onDismiss = {},
        onClearError = {},
    )
}

@Preview
@Composable
fun ApprovalDialogSupremeCourtModePreview() {
    val request =
        ApprovalRequest(
            id = "req-2",
            approvalType = "supreme_court",
            options = listOf("uphold", "overturn", "redesign"),
            context =
                ApprovalContext(
                    eta = "10m",
                    splitProposal = "Split into 3 sub-tasks",
                    detail = "Review pipeline strategy",
                ),
            timeoutSec = 300,
        )
    ApprovalDialog(
        approval = request,
        remainingTimeSec = 180,
        isTimedOut = false,
        isSubmitting = false,
        error = null,
        onRespond = {},
        onDismiss = {},
        onClearError = {},
    )
}

@Preview
@Composable
fun ApprovalDialogTimedOutPreview() {
    val request =
        ApprovalRequest(
            id = "req-3",
            approvalType = "strategy",
            options = listOf("split_execute", "no_split", "cancel"),
            context = ApprovalContext(eta = "5m", detail = "Deploy to production"),
            timeoutSec = 300,
        )
    ApprovalDialog(
        approval = request,
        remainingTimeSec = 0,
        isTimedOut = true,
        isSubmitting = false,
        error = null,
        onRespond = {},
        onDismiss = {},
        onClearError = {},
    )
}
