package com.a303.helpmet.presentation.feature.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StreamingNoticeState {
    @SerialName("default") Default,
    @SerialName("caution") Caution,
    @SerialName("danger") Danger,
}

class NavigationViewModel() : ViewModel()  {
    private val _isActiveStreamingView = MutableStateFlow(true)
    val isActiveStreamingView: StateFlow<Boolean> = _isActiveStreamingView

    private val _noticeState = MutableStateFlow(StreamingNoticeState.Default)
    val noticeState: StateFlow<StreamingNoticeState> = _noticeState
    private var noticeResetJob: Job? = null

    // 토글 함수
    fun toggleStreaming() {
        _isActiveStreamingView.value = !_isActiveStreamingView.value
    }

    // 안내 멘트 업데이트 함수
    fun updateNoticeState(state: StreamingNoticeState) {
        // 동일 상태로 반복 호출되는 것 방지
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
}