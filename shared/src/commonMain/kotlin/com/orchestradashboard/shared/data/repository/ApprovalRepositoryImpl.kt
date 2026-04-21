package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.orchestrator.ApprovalRequestDto
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.repository.ApprovalRepository

class ApprovalRepositoryImpl(
    private val api: DashboardApi,
) : ApprovalRepository {
    override suspend fun respondToApproval(
        approvalId: String,
        decision: String,
        comment: String,
    ): Result<Unit> =
        runCatching {
            api.respondToApproval(approvalId, ApprovalRequestDto(decision = decision, comment = comment))
        }
}
