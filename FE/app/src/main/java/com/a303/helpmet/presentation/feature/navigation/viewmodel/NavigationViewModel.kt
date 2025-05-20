package com.a303.helpmet.presentation.feature.navigation.viewmodel

import DeviceProvider
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.data.repository.DeviceRepository
import com.a303.helpmet.data.repository.WebsocketRepository
import com.a303.helpmet.data.service.DeviceService
import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.domain.usecase.GetWifiNetworkUseCase
import com.a303.helpmet.presentation.state.DirectionStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val deviceRepository: DeviceRepository,
    private val websocketRepository: WebsocketRepository,
    private val getWifiNetworkUseCase: GetWifiNetworkUseCase
) : ViewModel()  {
    private val _isActiveStreamingView = MutableStateFlow(true)
    val isActiveStreamingView: StateFlow<Boolean> = _isActiveStreamingView

    val directionState: StateFlow<DirectionState> = DirectionStateManager.directionState

    private var isSocketConnected = false

    // 토글 함수
    fun toggleStreaming() {
        _isActiveStreamingView.value = !_isActiveStreamingView.value
    }

    private val _isValidPi = MutableStateFlow<Boolean?>(null)
    val isValidPi: StateFlow<Boolean?> = _isValidPi

    private val _isAccessState = MutableStateFlow(false);
    val isAccessible: StateFlow<Boolean> = _isAccessState


    private fun validateDevice(baseUrl: String, onValidated: (Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            val retrofit = getWifiNetworkUseCase()
                ?.let { DeviceProvider.create(baseUrl, it) }
            val service = retrofit?.create(DeviceService::class.java)
            val repository = service?.let { DeviceRepository(it) }

            if (repository != null) {
                Log.d("qwer", "1${isValidPi.value} ${isAccessible.value}")

                val (isValidPi, isAccess) = repository.isHelpmetDevice()
                Log.d("qwer", "${isValidPi} ${isAccess}")
                _isValidPi.value = isValidPi
                _isAccessState.value = isAccess

                onValidated(isValidPi, isAccess)
            }

        }
    }

    fun connectToSocket(baseUrl: String, onFrameReceived: (Bitmap) -> Unit) {
        if (!isSocketConnected){
            validateDevice(baseUrl) { isValidPi, isAccess ->
                if (isValidPi && isAccess) {
                    Log.d("qwer", "연결 시작")
                    websocketRepository.connect(onFrameReceived)
                    isSocketConnected = true
                }
            }

        }

    }

    fun disconnectFromSocket() {
        if (isSocketConnected) {
            websocketRepository.disconnect()
            isSocketConnected = false
        }
    }
}