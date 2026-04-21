package com.orchestradashboard.shared.ui.pipelinemonitor

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.domain.model.ApprovalDecisionValue
import com.orchestradashboard.shared.domain.model.ConnectionStatus
import com.orchestradashboard.shared.domain.model.GenericDecision
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import com.orchestradashboard.shared.domain.repository.PipelineMonitorRepository
import com.orchestradashboard.shared.ui.approvalmodal.ApprovalModalViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

private const val MAX_LOG_LINES = 500

class PipelineMonitorViewModel(
    private val pipelineId: String,
    private val repository: PipelineMonitorRepository,
    val approvalModal: ApprovalModalViewModel = ApprovalModalViewModel(),
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _pipelineState = MutableStateFlow(PipelineMonitorUiState())
    val uiState: StateFlow<PipelineMonitorUiState> =
        combine(_pipelineState, approvalModal.uiState) { pipelineState, approvalState ->
            pipelineState.copy(
                pendingApproval = approvalState.pendingApproval,
                approvalRemainingTimeSec = approvalState.remainingTimeSec,
                isApprovalTimedOut = approvalState.isTimedOut,
                isApprovalSubmitting = approvalState.isSubmitting,
                approvalError = approvalState.error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PipelineMonitorUiState(),
        )

    fun loadPipeline() {
        viewModelScope.launch {
            _pipelineState.update { it.copy(isLoading = true, error = null) }
            repository.getPipelineDetail(pipelineId)
                .onSuccess { newPipeline ->
                    val oldSteps = _pipelineState.value.pipeline?.steps
                    val mergedSteps =
                        if (oldSteps != null) {
                            newPipeline.steps.map { newStep ->
                                val oldStep = oldSteps.firstOrNull { it.name == newStep.name }
                                if (newStep.status == StepStatus.RUNNING &&
                                    oldStep?.status == StepStatus.RUNNING &&
                                    oldStep.startedAtMs != null
                                ) {
                                    newStep.copy(startedAtMs = oldStep.startedAtMs)
                                } else {
                                    newStep
                                }
                            }
                        } else {
                            newPipeline.steps
                        }
                    _pipelineState.update { it.copy(pipeline = newPipeline.copy(steps = mergedSteps), isLoading = false) }
                    if (newPipeline.isParallel) {
                        loadParallelPipelines()
                    }
                }
                .onFailure { e ->
                    _pipelineState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun loadParallelPipelines() {
        viewModelScope.launch {
            repository.getParallelPipelines(pipelineId)
                .onSuccess { group ->
                    _pipelineState.update {
                        it.copy(
                            parallelGroup = group,
                            parallelPipelines = group.pipelines,
                        )
                    }
                }
                .onFailure { e ->
                    _pipelineState.update { it.copy(error = e.message) }
                }
        }
    }

    fun startObserving() {
        viewModelScope.launch {
            _pipelineState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTED) }
            repository.observePipelineEvents(pipelineId)
                .catch { e ->
                    _pipelineState.update {
                        it.copy(connectionStatus = ConnectionStatus.DISCONNECTED, error = e.message)
                    }
                }
                .collect { event -> handleEvent(event) }
        }
    }

    private fun handleEvent(event: PipelineEventDto) {
        val laneId = event.laneId
        if (laneId != null) {
            handleLaneEvent(laneId, event)
            return
        }
        when (event.type) {
            "step.started" -> updateStepStatus(event.step, StepStatus.RUNNING, event.elapsedSec)
            "step.completed" -> updateStepStatus(event.step, StepStatus.PASSED, event.elapsedSec)
            "step.failed" -> updateStepStatus(event.step, StepStatus.FAILED, event.elapsedSec)
            "pipeline.completed" -> updatePipelineStatus(PipelineRunStatus.PASSED)
            "pipeline.failed" -> updatePipelineStatus(PipelineRunStatus.FAILED)
            "approval.requested", "supreme_court.required" -> approvalModal.onApprovalRequested(event)
            "log" -> {
                event.detail?.let { line ->
                    _pipelineState.update { state ->
                        val updated = state.logLines.toMutableList().apply { add(line) }
                        state.copy(logLines = if (updated.size > MAX_LOG_LINES) updated.takeLast(MAX_LOG_LINES) else updated)
                    }
                }
            }
        }
    }

    private fun handleLaneEvent(
        laneId: String,
        event: PipelineEventDto,
    ) {
        _pipelineState.update { state ->
            val group = state.parallelGroup ?: return@update state
            val updatedPipelines =
                group.pipelines.map { lane ->
                    if (lane.id == laneId) applyEventToLane(lane, event) else lane
                }
            val updatedGroup = group.copy(pipelines = updatedPipelines)
            state.copy(
                parallelGroup = updatedGroup,
                parallelPipelines = updatedGroup.pipelines,
            )
        }
    }

    private fun applyEventToLane(
        lane: MonitoredPipeline,
        event: PipelineEventDto,
    ): MonitoredPipeline =
        when (event.type) {
            "step.started", "step.completed", "step.failed" -> {
                val stepName = event.step ?: return lane
                val newStatus =
                    when (event.type) {
                        "step.started" -> StepStatus.RUNNING
                        "step.completed" -> StepStatus.PASSED
                        else -> StepStatus.FAILED
                    }
                val updatedSteps =
                    lane.steps.map { step ->
                        if (step.name == stepName) {
                            step.copy(
                                status = newStatus,
                                elapsedMs = event.elapsedSec?.let { (it * 1000).toLong() } ?: step.elapsedMs,
                                startedAtMs =
                                    if (newStatus == StepStatus.RUNNING) {
                                        Clock.System.now().toEpochMilliseconds()
                                    } else {
                                        null
                                    },
                            )
                        } else {
                            step
                        }
                    }
                lane.copy(steps = updatedSteps)
            }
            "pipeline.completed" -> lane.copy(status = PipelineRunStatus.PASSED)
            "pipeline.failed" -> lane.copy(status = PipelineRunStatus.FAILED)
            else -> lane
        }

    private fun updateStepStatus(
        stepName: String?,
        status: StepStatus,
        elapsedSec: Double?,
    ) {
        if (stepName == null) return
        _pipelineState.update { state ->
            val pipeline = state.pipeline ?: return@update state
            val updatedSteps =
                pipeline.steps.map { step ->
                    if (step.name == stepName) {
                        step.copy(
                            status = status,
                            elapsedMs = elapsedSec?.let { (it * 1000).toLong() } ?: step.elapsedMs,
                            startedAtMs =
                                if (status == StepStatus.RUNNING) {
                                    Clock.System.now().toEpochMilliseconds()
                                } else {
                                    null
                                },
                        )
                    } else {
                        step
                    }
                }
            state.copy(pipeline = pipeline.copy(steps = updatedSteps))
        }
    }

    private fun updatePipelineStatus(status: PipelineRunStatus) {
        _pipelineState.update { state ->
            state.copy(pipeline = state.pipeline?.copy(status = status))
        }
    }

    fun clearError() {
        _pipelineState.update { it.copy(error = null) }
    }

    fun respondToApproval(
        decision: ApprovalDecisionValue,
        comment: String = "",
    ) {
        approvalModal.respond(GenericDecision(decision.value), comment)
    }

    fun dismissApproval() {
        approvalModal.dismiss()
    }

    fun clearApprovalError() {
        approvalModal.clearError()
    }

    fun refresh() {
        loadPipeline()
    }

    fun onCleared() {
        viewModelScope.cancel()
        approvalModal.onCleared()
    }
}
