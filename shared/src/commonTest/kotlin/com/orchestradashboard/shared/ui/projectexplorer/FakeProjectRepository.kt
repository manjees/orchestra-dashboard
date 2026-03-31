package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.repository.ProjectRepository

class FakeProjectRepository : ProjectRepository {
    var projectsResult: Result<List<Project>> = Result.success(emptyList())
    var issuesResult: Result<List<Issue>> = Result.success(emptyList())

    var getProjectsCallCount = 0
        private set
    var getProjectIssuesCallCount = 0
        private set
    var lastRequestedProjectName: String? = null
        private set

    override suspend fun getProjects(): Result<List<Project>> {
        getProjectsCallCount++
        return projectsResult
    }

    override suspend fun getProjectIssues(projectName: String): Result<List<Issue>> {
        getProjectIssuesCallCount++
        lastRequestedProjectName = projectName
        return issuesResult
    }
}
