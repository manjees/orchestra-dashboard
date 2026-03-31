package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.repository.SystemEventData
import com.orchestradashboard.shared.ui.dashboardhome.FakeSystemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSystemEventsUseCaseTest {
    private val repository = FakeSystemRepository()
    private val useCase = ObserveSystemEventsUseCase(repository)

    @Test
    fun `invoke returns flow from repository`() =
        runTest {
            val event = SystemEventData(ramPercent = 80.0, cpuPercent = 60.0, thermal = "moderate")

            val collected = mutableListOf<SystemEventData>()
            val job =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    useCase().collect { collected.add(it) }
                }

            repository.eventsFlow.emit(event)

            assertEquals(1, collected.size)
            assertEquals(80.0, collected[0].ramPercent)
            assertEquals(60.0, collected[0].cpuPercent)

            job.cancel()
        }

    @Test
    fun `flow emits events from WebSocket`() =
        runTest {
            val event1 = SystemEventData(ramPercent = 70.0)
            val event2 = SystemEventData(cpuPercent = 55.0)

            val collected = mutableListOf<SystemEventData>()
            val job =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    useCase().collect { collected.add(it) }
                }

            repository.eventsFlow.emit(event1)
            repository.eventsFlow.emit(event2)

            assertEquals(2, collected.size)
            assertEquals(70.0, collected[0].ramPercent)
            assertEquals(55.0, collected[1].cpuPercent)

            job.cancel()
        }
}
