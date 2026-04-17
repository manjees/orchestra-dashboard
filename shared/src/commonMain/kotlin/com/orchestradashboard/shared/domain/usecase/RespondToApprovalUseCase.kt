package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.repository.ApprovalRepository

class RespondToApprovalUseCase(
    private val repository: ApprovalRepository,
) {
    suspend operator fun invoke(
        approvalId: String,
        decision: String,
        comment: String = "",
    ): Result<Unit> = repository.respondToApproval(approvalId, decision, comment)
}
