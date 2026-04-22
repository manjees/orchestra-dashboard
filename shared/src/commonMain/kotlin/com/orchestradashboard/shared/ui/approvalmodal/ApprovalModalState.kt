package com.orchestradashboard.shared.ui.approvalmodal

import com.orchestradashboard.shared.domain.model.ApprovalRequest

data class ApprovalModalState(
    val showDialog: Boolean = false,
    val pendingApproval: ApprovalRequest? = null,
    val remainingTimeSec: Int? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val isTimedOut: Boolean get() = remainingTimeSec != null && remainingTimeSec <= 0
}
