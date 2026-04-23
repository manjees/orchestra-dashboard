package com.orchestradashboard.shared.ui.settings

import com.orchestradashboard.shared.domain.model.AppSettings
import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.domain.repository.SettingsRepository
import com.orchestradashboard.shared.domain.usecase.GetNotificationSettingsUseCase
import com.orchestradashboard.shared.domain.usecase.GetSettingsUseCase
import com.orchestradashboard.shared.domain.usecase.SaveNotificationSettingsUseCase
import com.orchestradashboard.shared.domain.usecase.SaveSettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private class FakeSettingsRepository(
        var baseUrl: String = "http://default",
        var apiKey: String = "",
    ) : SettingsRepository {
        private val flow = MutableStateFlow(AppSettings(baseUrl, apiKey))

        override suspend fun getBaseUrl(): String = baseUrl

        override suspend fun saveBaseUrl(url: String) {
            baseUrl = url
            flow.value = AppSettings(baseUrl, apiKey)
        }

        override suspend fun getApiKey(): String = apiKey

        override suspend fun saveApiKey(key: String) {
            apiKey = key
            flow.value = AppSettings(baseUrl, apiKey)
        }

        override fun observeSettings(): Flow<AppSettings> = flow
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        notificationRepo: FakeNotificationRepository = FakeNotificationRepository(),
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
    ): Pair<SettingsViewModel, FakeNotificationRepository> {
        val vm =
            SettingsViewModel(
                getSettingsUseCase = GetSettingsUseCase(settingsRepo),
                saveSettingsUseCase = SaveSettingsUseCase(settingsRepo),
                getNotificationSettingsUseCase = GetNotificationSettingsUseCase(notificationRepo),
                saveNotificationSettingsUseCase = SaveNotificationSettingsUseCase(notificationRepo),
            )
        return vm to notificationRepo
    }

    @Test
    fun `loadSettings populates notification settings from repository`() =
        runTest(testDispatcher) {
            val repo =
                FakeNotificationRepository().apply {
                    currentSettings =
                        NotificationSettings(
                            enabled = false,
                            notifyOnSuccess = true,
                            notifyOnFailure = false,
                        )
                }
            val (vm, _) = createViewModel(notificationRepo = repo)

            vm.loadSettings()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertFalse(state.notificationsEnabled)
            assertTrue(state.notifyOnSuccess)
            assertFalse(state.notifyOnFailure)
        }

    @Test
    fun `toggleNotifications updates state and persists`() =
        runTest(testDispatcher) {
            val (vm, repo) = createViewModel()
            vm.loadSettings()
            advanceUntilIdle()

            vm.toggleNotifications(enabled = false)
            advanceUntilIdle()

            assertFalse(vm.uiState.value.notificationsEnabled)
            assertFalse(repo.currentSettings.enabled)
        }

    @Test
    fun `toggleNotifyOnSuccess persists and updates state`() =
        runTest(testDispatcher) {
            val (vm, repo) = createViewModel()
            vm.loadSettings()
            advanceUntilIdle()

            vm.toggleNotifyOnSuccess(enabled = false)
            advanceUntilIdle()

            assertFalse(vm.uiState.value.notifyOnSuccess)
            assertFalse(repo.currentSettings.notifyOnSuccess)
        }

    @Test
    fun `toggleNotifyOnFailure persists and updates state`() =
        runTest(testDispatcher) {
            val (vm, repo) = createViewModel()
            vm.loadSettings()
            advanceUntilIdle()

            vm.toggleNotifyOnFailure(enabled = false)
            advanceUntilIdle()

            assertFalse(vm.uiState.value.notifyOnFailure)
            assertFalse(repo.currentSettings.notifyOnFailure)
        }

    @Test
    fun `saveSettings persists both base settings and notification settings`() =
        runTest(testDispatcher) {
            val settingsRepo = FakeSettingsRepository(baseUrl = "http://old")
            val (vm, notifRepo) =
                createViewModel(settingsRepo = settingsRepo)
            vm.loadSettings()
            advanceUntilIdle()

            vm.updateBaseUrl("http://new:8080")
            vm.toggleNotifyOnFailure(enabled = false)
            advanceUntilIdle()

            vm.saveSettings()
            advanceUntilIdle()

            assertTrue(vm.uiState.value.saveSuccess)
            assertEquals("http://new:8080", settingsRepo.baseUrl)
            assertFalse(notifRepo.currentSettings.notifyOnFailure)
        }
}
