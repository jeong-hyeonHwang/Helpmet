package com.a303.helpmet.presentation.state

import com.a303.helpmet.domain.model.DetectedObjectState
import com.a303.helpmet.domain.model.StreamingNoticeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


object DetectionStateManager {
    private val _noticeState = MutableStateFlow(StreamingNoticeState.Default)
    val noticeState: StateFlow<StreamingNoticeState> = _noticeState

    private val _detectedObjectState = MutableStateFlow(DetectedObjectState.Default)
    val detectedObjectState: StateFlow<DetectedObjectState> = _detectedObjectState

    // 안내 멘트 업데이트 함수
    private var noticeResetJob: Job? = null

    fun updateNoticeState(type: String, level: Int) {
        _detectedObjectState.value = setType(type)
        val newNotice = setLevel(level)

        if (newNotice == StreamingNoticeState.Danger || newNotice == StreamingNoticeState.Caution) {
            noticeResetJob?.cancel()
            _noticeState.value = newNotice

            noticeResetJob = CoroutineScope(Dispatchers.Default).launch {
                delay(5000)
                _noticeState.value = StreamingNoticeState.Default
            }
        }
    }

    private fun setLevel(level: Int?): StreamingNoticeState {
        return when (level) {
            1 -> StreamingNoticeState.Caution
            2 -> StreamingNoticeState.Danger
            else -> StreamingNoticeState.Default
        }
    }

    private fun setType(type: String?): DetectedObjectState {
        return when (type) {
            "PERSON_DETECTED" -> DetectedObjectState.Person
            "BICYCLE_DETECTED" -> DetectedObjectState.Bicycle
            "CAR_DETECTED" -> DetectedObjectState.Car
            else -> DetectedObjectState.Default
        }
    }
}
