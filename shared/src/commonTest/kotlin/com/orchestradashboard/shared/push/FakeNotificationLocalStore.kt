package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNotificationLocalStore(
    initial: NotificationSettings = NotificationSettings(),
) : NotificationLocalStore {
    private val state = MutableStateFlow(initial)

    var loadCallCount: Int = 0
        private set
    var saveCallCount: Int = 0
        private set

    override suspend fun load(): NotificationSettings {
        loadCallCount++
        return state.value
    }

    override suspend fun save(settings: NotificationSettings) {
        saveCallCount++
        state.value = settings
    }

    override fun observe(): Flow<NotificationSettings> = state
}
