import SwiftUI

/// Settings screen for configuring Orchestrator connection.
/// Provides Base URL and API Key input fields with save functionality.
struct SettingsView: View {
    @StateObject private var viewModel = IOSSettingsViewModel()

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Orchestrator Connection")) {
                    TextField("Base URL", text: Binding(
                        get: { viewModel.baseUrl },
                        set: { viewModel.updateBaseUrl($0) }
                    ))
                    .keyboardType(.URL)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)

                    SecureField("API Key", text: Binding(
                        get: { viewModel.apiKey },
                        set: { viewModel.updateApiKey($0) }
                    ))
                }

                Section {
                    Button(action: { viewModel.saveSettings() }) {
                        HStack {
                            if viewModel.isSaving {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle())
                            }
                            Text("Save Settings")
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .disabled(viewModel.isSaving || viewModel.isLoading)
                }

                if viewModel.saveSuccess {
                    Section {
                        HStack {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundStyle(.green)
                            Text("Settings saved successfully.")
                                .foregroundStyle(.green)
                        }
                    }
                }

                Section(header: Text("About")) {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text("1.0.0")
                            .foregroundStyle(.secondary)
                    }
                }

                // Phase 3: Push notification settings will be added here
            }
            .navigationTitle("Settings")
        }
        .onAppear { viewModel.loadSettings() }
        .onDisappear { viewModel.onCleared() }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") { viewModel.clearError() }
        } message: {
            Text(viewModel.error ?? "")
        }
    }
}

#Preview {
    SettingsView()
}
