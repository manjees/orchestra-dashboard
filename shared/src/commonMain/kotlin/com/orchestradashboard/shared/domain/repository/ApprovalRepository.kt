package com.orchestradashboard.shared.domain.repository

interface ApprovalRepository {
    suspend fun respondToApproval(
        approvalId: String,
        decision: String,
        comment: String = "",
    ): Result<Unit>
}
