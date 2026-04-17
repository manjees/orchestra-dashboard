package com.orchestradashboard.android.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.orchestradashboard.shared.domain.model.NetworkStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Monitors network connectivity on Android using ConnectivityManager.
 * Emits [NetworkStatus] changes as a Flow.
 */
class AndroidNetworkMonitor(private val context: Context) {
    fun observe(): Flow<NetworkStatus> =
        callbackFlow {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        trySend(NetworkStatus.Available)
                    }

                    override fun onLost(network: Network) {
                        trySend(NetworkStatus.Unavailable)
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities,
                    ) {
                        val hasInternet =
                            networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_INTERNET,
                            )
                        trySend(
                            if (hasInternet) NetworkStatus.Available else NetworkStatus.Unavailable,
                        )
                    }
                }

            val request =
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

            connectivityManager.registerNetworkCallback(request, callback)

            // Emit initial state
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            val isConnected =
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            trySend(if (isConnected) NetworkStatus.Available else NetworkStatus.Unavailable)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
}
