package com.a303.helpmet.domain.usecase

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

class GetWifiNetworkUseCase(
    private val context: Context
) {
    operator fun invoke(): Network? {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

        return connectivityManager.activeNetwork?.takeIf { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }
    }
}