package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.DesignResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import com.orchestradashboard.shared.domain.model.CommandResult
import com.orchestradashboard.shared.domain.model.DesignResult
import com.orchestradashboard.shared.domain.model.DiscussResult
import com.orchestradashboard.shared.domain.model.PlanIssuesResult
import com.orchestradashboard.shared.domain.model.PlannedIssue
import com.orchestradashboard.shared.domain.model.ShellResult

class CommandMapper {
    fun mapInitResponse(dto: InitProjectResponseDto): CommandResult =
        CommandResult(
            success = dto.success,
            message = dto.message,
            pipelineId = dto.pipelineId,
        )

    fun mapPlanResponse(dto: PlanIssuesResponseDto): PlanIssuesResult =
        PlanIssuesResult(
            issues = dto.issues.map { PlannedIssue(it.title, it.body, it.labels) },
        )

    fun mapDiscussResponse(dto: DiscussResponseDto): DiscussResult =
        DiscussResult(
            answer = dto.answer,
            suggestedIssues = dto.suggestedIssues.map { PlannedIssue(it.title, it.body, it.labels) },
        )

    fun mapDesignResponse(dto: DesignResponseDto): DesignResult =
        DesignResult(
            spec = dto.spec,
            suggestedIssues = dto.suggestedIssues.map { PlannedIssue(it.title, it.body, it.labels) },
        )

    fun mapShellResponse(dto: ShellResponseDto): ShellResult =
        ShellResult(
            output = dto.output,
            exitCode = dto.exitCode,
        )
}
