import Foundation
import Shared

/// iOS-specific wrapper around the KMP SettingsViewModel.
/// Bridges Kotlin coroutine StateFlow to Swift's ObservableObject/Combine.
@MainActor
final class IOSSettingsViewModel: ObservableObject {
    @Published var baseUrl: String = ""
    @Published var apiKey: String = ""
    @Published var isSaving: Bool = false
    @Published var isLoading: Bool = false
    @Published var saveSuccess: Bool = false
    @Published var error: String? = nil

    private let kmpViewModel: SettingsViewModel
    private var stateCollectionTask: Task<Void, Never>?

    init() {
        let container = IOSAppContainer.shared
        self.kmpViewModel = container.createSettingsViewModel()
        startCollectingState()
    }

    private func startCollectingState() {
        stateCollectionTask = Task { @MainActor [weak self] in
            guard let self else { return }
            for await state in kmpViewModel.uiState {
                self.baseUrl = state.baseUrl
                self.apiKey = state.apiKey
                self.isSaving = state.isSaving
                self.isLoading = state.isLoading
                self.saveSuccess = state.saveSuccess
                self.error = state.error
            }
        }
    }

    func loadSettings() {
        kmpViewModel.loadSettings()
    }

    func updateBaseUrl(_ url: String) {
        kmpViewModel.updateBaseUrl(url: url)
    }

    func updateApiKey(_ key: String) {
        kmpViewModel.updateApiKey(key: key)
    }

    func saveSettings() {
        kmpViewModel.saveSettings()
    }

    func clearError() {
        kmpViewModel.clearError()
    }

    func onCleared() {
        stateCollectionTask?.cancel()
        kmpViewModel.onCleared()
    }
}
