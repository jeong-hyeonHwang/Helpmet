package com.a303.helpmet.presentation.state.detection

import com.a303.helpmet.domain.model.DetectedObjectState
import com.a303.helpmet.domain.model.DetectionNoticeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


object DetectionNoticeStateManager {
    private val _noticeState = MutableStateFlow(DetectionNoticeState.Default)
    val noticeState: StateFlow<DetectionNoticeState> = _noticeState

    private val _detectedObjectState = MutableStateFlow(DetectedObjectState.Default)
    val detectedObjectState: StateFlow<DetectedObjectState> = _detectedObjectState

    // 안내 멘트 업데이트 함수
    private var noticeResetJob: Job? = null

    fun updateNoticeState(type: String, level: Int) {
        _detectedObjectState.value = setType(type)
        val newNotice = setLevel(level)

        if (newNotice == DetectionNoticeState.Danger || newNotice == DetectionNoticeState.Caution) {
            noticeResetJob?.cancel()
            _noticeState.value = newNotice

            noticeResetJob = CoroutineScope(Dispatchers.Default).launch {
                delay(5000)
                _noticeState.value = DetectionNoticeState.Default
            }
        }
    }

    private fun setLevel(level: Int?): DetectionNoticeState {
        return when (level) {
            1 -> DetectionNoticeState.Caution
            2 -> DetectionNoticeState.Danger
            else -> DetectionNoticeState.Default
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
