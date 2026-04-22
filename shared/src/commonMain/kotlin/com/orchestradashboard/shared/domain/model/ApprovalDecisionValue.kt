package com.orchestradashboard.shared.domain.model

enum class ApprovalDecisionValue(val value: String) {
    SplitExecute("split_execute"),
    NoSplit("no_split"),
    Cancel("cancel"),
    Uphold("uphold"),
    Overturn("overturn"),
    Redesign("redesign"),
    Approve("approve"),
    Reject("reject"),
}
