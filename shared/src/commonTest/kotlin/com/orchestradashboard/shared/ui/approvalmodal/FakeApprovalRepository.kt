package com.orchestradashboard.shared.ui.approvalmodal

import com.orchestradashboard.shared.domain.repository.ApprovalRepository

class FakeApprovalRepository : ApprovalRepository {
    var respondResult: Result<Unit> = Result.success(Unit)
    var respondCallCount = 0
        private set
    var lastApprovalId: String? = null
        private set
    var lastDecision: String? = null
        private set
    var lastComment: String? = null
        private set

    override suspend fun respondToApproval(
        approvalId: String,
        decision: String,
        comment: String,
    ): Result<Unit> {
        respondCallCount++
        lastApprovalId = approvalId
        lastDecision = decision
        lastComment = comment
        return respondResult
    }
}
