package com.a303.helpmet.presentation.feature.navigation.viewmodel

import DeviceProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.data.repository.DeviceRepository
import com.a303.helpmet.data.repository.DirectionSocketRepository
import com.a303.helpmet.data.service.DeviceService
import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.domain.model.StreamingNoticeState
import com.a303.helpmet.presentation.state.DirectionStateManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val deviceRepository: DeviceRepository,
    private val directionSocketRepository: DirectionSocketRepository
) : ViewModel()  {
    private val _isActiveStreamingView = MutableStateFlow(true)
    val isActiveStreamingView: StateFlow<Boolean> = _isActiveStreamingView

    private val _noticeState = MutableStateFlow(StreamingNoticeState.Default)
    val noticeState: StateFlow<StreamingNoticeState> = _noticeState

    val directionState: StateFlow<DirectionState> = DirectionStateManager.directionState

    private var noticeResetJob: Job? = null
    private var isSocketConnected = false

    // 토글 함수
    fun toggleStreaming() {
        _isActiveStreamingView.value = !_isActiveStreamingView.value
    }

    // 안내 멘트 업데이트 함수
    fun updateNoticeState(state: StreamingNoticeState) {
        if (_noticeState.value == state) return

        if (state == StreamingNoticeState.Danger || state == StreamingNoticeState.Caution) {
            noticeResetJob?.cancel()
            _noticeState.value = state

            noticeResetJob = viewModelScope.launch {
                delay(5000)
                _noticeState.value = StreamingNoticeState.Default
            }
        }
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

    fun connectToDirectionSocket() {
        if (!isSocketConnected){
            directionSocketRepository.connect()
            isSocketConnected = true
        }

    }

    fun disconnectFromDirectionSocket() {
        if (isSocketConnected) {
            directionSocketRepository.disconnect()
            isSocketConnected = false
        }
    }
}