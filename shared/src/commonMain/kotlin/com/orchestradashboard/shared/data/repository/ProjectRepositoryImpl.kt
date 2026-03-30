package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.OrchestratorApi
import com.orchestradashboard.shared.data.mapper.CheckpointMapper
import com.orchestradashboard.shared.data.mapper.IssueMapper
import com.orchestradashboard.shared.data.mapper.ProjectMapper
import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.repository.ProjectRepository

class ProjectRepositoryImpl(
    private val api: OrchestratorApi,
    private val projectMapper: ProjectMapper,
    private val issueMapper: IssueMapper,
    private val checkpointMapper: CheckpointMapper,
) : ProjectRepository {
    override suspend fun getProjects(): Result<List<Project>> =
        runCatching {
            projectMapper.toDomain(api.getProjects())
        }

    override suspend fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): Result<List<Issue>> =
        runCatching {
            issueMapper.toDomain(api.getProjectIssues(name, page, pageSize))
        }

    override suspend fun getCheckpoints(): Result<List<Checkpoint>> =
        runCatching {
            checkpointMapper.toDomain(api.getCheckpoints())
        }
}
