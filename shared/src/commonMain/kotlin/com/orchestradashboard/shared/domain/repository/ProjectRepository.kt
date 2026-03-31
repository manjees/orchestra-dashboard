package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project

interface ProjectRepository {
    suspend fun getProjects(): Result<List<Project>>

    suspend fun getProjectIssues(
        name: String,
        page: Int = 0,
        pageSize: Int = 20,
    ): Result<List<Issue>>

    suspend fun getCheckpoints(): Result<List<Checkpoint>>
}
