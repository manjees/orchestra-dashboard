package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.ShellResult
import com.orchestradashboard.shared.domain.repository.CommandRepository

class ExecuteShellUseCase(private val repository: CommandRepository) {
    suspend operator fun invoke(command: String): Result<ShellResult> = repository.executeShell(command)
}
