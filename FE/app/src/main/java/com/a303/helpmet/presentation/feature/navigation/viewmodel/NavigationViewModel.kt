package com.a303.helpmet.presentation.feature.navigation.viewmodel

import DeviceProvider
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.data.repository.DeviceRepository
import com.a303.helpmet.data.repository.WebsocketRepository
import com.a303.helpmet.data.service.DeviceService
import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.presentation.state.DirectionStateManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val deviceRepository: DeviceRepository,
    private val websocketRepository: WebsocketRepository
) : ViewModel()  {
    private val _isActiveStreamingView = MutableStateFlow(true)
    val isActiveStreamingView: StateFlow<Boolean> = _isActiveStreamingView

    val directionState: StateFlow<DirectionState> = DirectionStateManager.directionState

    private var noticeResetJob: Job? = null
    private var isSocketConnected = false

    // 토글 함수
    fun toggleStreaming() {
        _isActiveStreamingView.value = !_isActiveStreamingView.value
    }

    private val _isValidPi = MutableStateFlow<Boolean?>(null)
    val isValidPi: StateFlow<Boolean?> = _isValidPi

    fun validateDevice(baseUrl: String) {
        viewModelScope.launch {
            val retrofit = DeviceProvider.create(baseUrl)
            val service = retrofit.create(DeviceService::class.java)
            val repository = DeviceRepository(service)

            _isValidPi.value = repository.isHelpmetDevice()
        }
    }

    fun connectToSocket(onFrameReceived: (Bitmap) -> Unit) {
        if (!isSocketConnected){
            websocketRepository.connect(onFrameReceived)
            isSocketConnected = true
        }

    }

    fun disconnectFromSocket() {
        if (isSocketConnected) {
            websocketRepository.disconnect()
            isSocketConnected = false
        }
    }
}