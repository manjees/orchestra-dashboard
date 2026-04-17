import SwiftUI

/// Displays an offline banner when network is unavailable.
struct NetworkStatusBannerView: View {
    let isConnected: Bool

    var body: some View {
        if !isConnected {
            HStack {
                Image(systemName: "wifi.slash")
                    .font(.footnote)
                Text("No internet connection")
                    .font(.footnote)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
            .padding(.horizontal, 16)
            .background(Color.red.opacity(0.15))
            .foregroundStyle(.red)
        }
    }
}

#Preview {
    VStack {
        NetworkStatusBannerView(isConnected: false)
        NetworkStatusBannerView(isConnected: true)
    }
}
