package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApprovalContextDto(
    val eta: String? = null,
    @SerialName("split_proposal") val splitProposal: String? = null,
    val detail: String? = null,
)
