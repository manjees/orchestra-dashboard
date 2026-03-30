import SwiftUI

/// Placeholder view for the Project Explorer screen.
/// Full implementation will bridge to the KMP shared ProjectExplorerViewModel.
struct ProjectExplorerView: View {
    var body: some View {
        VStack {
            Text("Project Explorer")
                .font(.largeTitle)
            Text("Coming soon — KMP integration pending.")
                .foregroundStyle(.secondary)
        }
        .navigationTitle("Project Explorer")
    }
}

#Preview {
    NavigationView {
        ProjectExplorerView()
    }
}
