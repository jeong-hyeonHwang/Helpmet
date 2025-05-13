package com.a303.helpmet.presentation.feature.helmetcheck

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiNetworkSpecifier
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.BuildConfig
import com.a303.helpmet.R
import com.a303.helpmet.presentation.model.HelmetConnectionState
import com.a303.helpmet.util.handler.WifiScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HelmetCheckViewModel : ViewModel() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected // 헬멧의 최종 연결 상태

    private val _toastShown = MutableStateFlow(false)
    val toastShown: StateFlow<Boolean> = _toastShown

    fun markToastShown() {
        _toastShown.value = true
    }

    fun markToastUnShown() {
        _toastShown.value = false
    }

    private val _helmetName = MutableStateFlow("SEARCHING HELPMET..") // 임시 헬멧 명
    val helmetName: StateFlow<String> = _helmetName
    fun setHelmetName(name: String){
        _helmetName.value = name
        _connectionState.value = HelmetConnectionState.Found
    }

    private val _connectionState = MutableStateFlow(HelmetConnectionState.Idle) // 헬멧 연결 상태(기본값: 안됨)
    val connectionState: StateFlow<HelmetConnectionState> = _connectionState

    fun startSearchAndScan(context: Context, onResult: (List<ScanResult>) -> Unit) {
        _connectionState.value = HelmetConnectionState.Searching
        val scanner = WifiScanner(context)
        scanner.onScanResult = { result ->
            onResult(result)
            scanner.unregister(context)
        }
        scanner.scan(context)
    }

    fun connectToHelmetAp(context: Context) {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(_helmetName.value)
            .setWpa2Passphrase(BuildConfig.HELPMET_PASSWORD)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try{
            connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectivityManager.bindProcessToNetwork(network)
                    viewModelScope.launch {
                        _connectionState.value = HelmetConnectionState.Connecting
                        delay(500)
                        _isConnected.value = true
                        _connectionState.value = HelmetConnectionState.Success
                    }
                }

                override fun onUnavailable() {
                    viewModelScope.launch {
                        _connectionState.value = HelmetConnectionState.Idle
                        _isConnected.value = false
                    }
                }
            })
        }catch (e: SecurityException){
            viewModelScope.launch {
                _connectionState.value = HelmetConnectionState.Idle
                _isConnected.value = false
                Toast.makeText(context, context.getString(R.string.dialog_connecting_error_helmet), Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun disconnectFromHelmetAp(context: Context) {
        _connectionState.value = HelmetConnectionState.Disconnecting
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.bindProcessToNetwork(null)  // 시스템 기본 네트워크로 되돌림
            viewModelScope.launch {
                cancelDialog()
                Toast.makeText(context, context.getString(R.string.dialog_disconnect_helmet), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                Toast.makeText(context, context.getString(R.string.dialog_diconnecting_error_helmet), Toast.LENGTH_SHORT).show()
                _connectionState.value = HelmetConnectionState.Idle
            }
        }
    }

    // 헬멧에 연결하지 않기(=다이얼로그 닫기)
    fun cancelDialog() {
        _isConnected.value = false
        _connectionState.value = HelmetConnectionState.Idle
    }



}