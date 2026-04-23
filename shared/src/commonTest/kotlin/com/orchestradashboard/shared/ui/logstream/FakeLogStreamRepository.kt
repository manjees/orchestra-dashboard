package com.orchestradashboard.shared.ui.logstream

import com.orchestradashboard.shared.domain.model.LogEntry
import com.orchestradashboard.shared.domain.repository.LogStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class FakeLogStreamRepository : LogStreamRepository {
    val logFlow = MutableSharedFlow<LogEntry>()
    private var flowToReturn: Flow<LogEntry> = logFlow

    var observeCallCount = 0
        private set

    var lastStepId: String? = null
        private set

    fun setErrorFlow(exception: Exception) {
        flowToReturn = flow { throw exception }
    }

    fun resetToNormalFlow() {
        flowToReturn = logFlow
    }

    override fun observeLogStream(stepId: String): Flow<LogEntry> {
        observeCallCount++
        lastStepId = stepId
        return flowToReturn
    }
}
