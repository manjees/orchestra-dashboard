package com.orchestradashboard.shared.domain.model

sealed interface ApprovalDecision {
    val value: String
}

enum class StrategyDecision(override val value: String) : ApprovalDecision {
    SplitExecute("split_execute"),
    NoSplit("no_split"),
    Cancel("cancel"),
}

enum class SupremeCourtDecision(override val value: String) : ApprovalDecision {
    Uphold("uphold"),
    Overturn("overturn"),
    Redesign("redesign"),
}

data class GenericDecision(override val value: String) : ApprovalDecision
