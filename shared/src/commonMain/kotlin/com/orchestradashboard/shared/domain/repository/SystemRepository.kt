package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.ActivePipeline
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.SystemStatus
import kotlinx.coroutines.flow.Flow

interface SystemRepository {
    suspend fun getSystemStatus(): Result<SystemStatus>

    suspend fun getActivePipelines(): Result<List<ActivePipeline>>

    suspend fun getPipelineHistory(): Result<List<PipelineResult>>

    fun observeSystemEvents(): Flow<SystemEventData>
}

data class SystemEventData(
    val ramPercent: Double? = null,
    val cpuPercent: Double? = null,
    val thermal: String? = null,
    val step: String? = null,
    val status: String? = null,
)
