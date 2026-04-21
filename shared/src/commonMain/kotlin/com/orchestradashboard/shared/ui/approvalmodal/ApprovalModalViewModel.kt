package com.orchestradashboard.shared.ui.approvalmodal

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.mapper.ApprovalMapper
import com.orchestradashboard.shared.domain.model.ApprovalDecision
import com.orchestradashboard.shared.domain.model.ApprovalRequest
import com.orchestradashboard.shared.domain.usecase.RespondToApprovalUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

private const val DEFAULT_TIMEOUT_SEC = 300
private const val COUNTDOWN_INTERVAL_MS = 1000L

class ApprovalModalViewModel(
    private val respondToApprovalUseCase: RespondToApprovalUseCase? = null,
    private val approvalMapper: ApprovalMapper = ApprovalMapper(),
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(ApprovalModalState())
    val uiState: StateFlow<ApprovalModalState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun onApprovalRequested(event: PipelineEventDto) {
        if (_uiState.value.pendingApproval != null) return

        val currentMs = nowMs()
        val timeoutSec = event.timeoutSec ?: DEFAULT_TIMEOUT_SEC
        val deadlineMs = currentMs + timeoutSec * 1000L

        val approval =
            ApprovalRequest(
                approvalType = event.approvalType ?: "unknown",
                options = event.options ?: emptyList(),
                id = event.approvalId ?: event.pipelineId ?: "",
                context = approvalMapper.toDomain(event.context),
                timeoutSec = timeoutSec,
                requestedAtMs = currentMs,
            )

        _uiState.update {
            it.copy(
                showDialog = true,
                pendingApproval = approval,
                remainingTimeSec = timeoutSec,
            )
        }
        startCountdown(deadlineMs)
    }

    fun respond(
        decision: ApprovalDecision,
        comment: String = "",
    ) {
        if (_uiState.value.isTimedOut) return
        val approvalId = _uiState.value.pendingApproval?.id ?: return

        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            respondToApprovalUseCase?.invoke(approvalId, decision.value, comment)
                ?.onSuccess {
                    clearApprovalState()
                }
                ?.onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun dismiss() {
        clearApprovalState()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }

    private fun startCountdown(deadlineMs: Long) {
        countdownJob?.cancel()
        countdownJob =
            viewModelScope.launch {
                while (true) {
                    delay(COUNTDOWN_INTERVAL_MS)
                    val remaining = maxOf(0, ((deadlineMs - nowMs()) / 1000).toInt())
                    _uiState.update { it.copy(remainingTimeSec = remaining) }
                    if (remaining <= 0) {
                        autoApprove()
                        break
                    }
                }
            }
    }

    private fun clearApprovalState() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                showDialog = false,
                pendingApproval = null,
                remainingTimeSec = null,
                isSubmitting = false,
            )
        }
    }

    private fun autoApprove() {
        val approvalId = _uiState.value.pendingApproval?.id ?: return
        viewModelScope.launch {
            respondToApprovalUseCase?.invoke(approvalId, "auto_approved")
                ?.onSuccess {
                    clearApprovalState()
                }
                ?.onFailure { e ->
                    clearApprovalState()
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
}
