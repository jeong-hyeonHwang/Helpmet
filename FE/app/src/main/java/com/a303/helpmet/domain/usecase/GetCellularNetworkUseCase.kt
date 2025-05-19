package com.a303.helpmet.domain.usecase

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class GetCellularNetworkUseCase(
    private val context: Context
) {
    suspend operator fun invoke(): Network? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            val cm = context.getSystemService(ConnectivityManager::class.java)
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            cm.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    // 여기서 전역 바인딩 수행
                    cm.bindProcessToNetwork(network)
                    cont.resume(network) {}
                }

                override fun onUnavailable() {
                    cont.resume(null) {}
                }
            })
        }
    }
}
