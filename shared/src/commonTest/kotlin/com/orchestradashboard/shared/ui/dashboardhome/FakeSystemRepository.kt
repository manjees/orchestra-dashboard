package com.orchestradashboard.shared.ui.dashboardhome

import com.orchestradashboard.shared.domain.model.ActivePipeline
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.SystemStatus
import com.orchestradashboard.shared.domain.model.ThermalPressure
import com.orchestradashboard.shared.domain.repository.SystemEventData
import com.orchestradashboard.shared.domain.repository.SystemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeSystemRepository : SystemRepository {
    var systemStatusResult: Result<SystemStatus> =
        Result.success(
            SystemStatus(
                ramPercent = 50.0,
                cpuPercent = 40.0,
                diskPercent = 60.0,
                thermalPressure = ThermalPressure.NOMINAL,
            ),
        )
    var activePipelinesResult: Result<List<ActivePipeline>> = Result.success(emptyList())
    var pipelineHistoryResult: Result<List<PipelineResult>> = Result.success(emptyList())
    val eventsFlow = MutableSharedFlow<SystemEventData>()

    var getSystemStatusCallCount = 0
        private set
    var getActivePipelinesCallCount = 0
        private set
    var getPipelineHistoryCallCount = 0
        private set

    override suspend fun getSystemStatus(): Result<SystemStatus> {
        getSystemStatusCallCount++
        return systemStatusResult
    }

    override suspend fun getActivePipelines(): Result<List<ActivePipeline>> {
        getActivePipelinesCallCount++
        return activePipelinesResult
    }

    override suspend fun getPipelineHistory(): Result<List<PipelineResult>> {
        getPipelineHistoryCallCount++
        return pipelineHistoryResult
    }

    override fun observeSystemEvents(): Flow<SystemEventData> = eventsFlow
}
