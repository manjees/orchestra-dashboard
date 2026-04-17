package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.ApprovalContextDto
import com.orchestradashboard.shared.domain.model.ApprovalContext

class ApprovalMapper {
    fun toDomain(dto: ApprovalContextDto?): ApprovalContext? {
        if (dto == null) return null
        return ApprovalContext(
            eta = dto.eta,
            splitProposal = dto.splitProposal,
            detail = dto.detail,
        )
    }
}
