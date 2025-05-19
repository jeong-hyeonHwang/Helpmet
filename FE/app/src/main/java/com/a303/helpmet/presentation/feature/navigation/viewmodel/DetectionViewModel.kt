package com.a303.helpmet.presentation.feature.navigation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.data.ml.analysis.ApproachAnalyzer
import com.a303.helpmet.data.ml.detector.YoloV5TFLiteDetector
import com.a303.helpmet.data.ml.tracker.SimpleTracker
import com.a303.helpmet.data.repository.WebsocketRepository
import com.a303.helpmet.domain.model.command.DetectionCommand
import com.a303.helpmet.presentation.state.detection.DetectionNoticeStateManager
import com.a303.helpmet.presentation.state.detection.PiAccessManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class DetectionViewModel(
    application: Application,
    private val websocketRepository: WebsocketRepository,
) : AndroidViewModel(application) {

    private val detector = try {
        YoloV5TFLiteDetector(application.applicationContext)
    } catch (e: Exception) {
        Log.e("DetectionViewModel", "detector 초기화 실패", e)
        null
    }

    private val analyzer = ApproachAnalyzer()
    private val tracker = SimpleTracker()
    private val _lastWarningLevels = mutableMapOf<Int, Int>()

    private val _latestBitmap = mutableStateOf<Bitmap?>(null)
    private val _isProcessing = mutableStateOf(false)

    private val classDictionary = mapOf(
        0 to "사람",
        1 to "자전거",
        2 to "자동차"
    )

    fun onFrameReceived(): (Bitmap) -> Unit = { bitmap ->
        _latestBitmap.value = bitmap
    }

    fun startDetectionLoop() {
        viewModelScope.launch {
            while (true) {
                if (!_isProcessing.value && _latestBitmap.value != null) {
                    val currentBitmap = _latestBitmap.value
                    _latestBitmap.value = null
                    _isProcessing.value = true
                    launch(Dispatchers.Default) {
                        try {
                            detector?.let {
                                detectAndSend(currentBitmap!!, it)
                            }
                        } catch (e: Exception) {
                            Log.e("Detector", "detect() 중 예외 발생", e)
                        } finally {
                            _isProcessing.value = false
                        }
                    }
                }
                delay(50)
            }
        }
    }

    private fun detectAndSend(bitmap: Bitmap, detector: YoloV5TFLiteDetector) {
        val results = detector.detect(bitmap)
        val rects = results.map { it.rect }
        val trackedMap = tracker.update(rects)

        trackedMap.forEach { (trackId, rect) ->
            val level = analyzer.getApproachLevel(trackId, rect)
            val prevLevel = _lastWarningLevels[trackId] ?: 0
            val matched = results.firstOrNull { it.rect == rect }

            matched?.let { result ->

                if (level > prevLevel) {
                    if(PiAccessManager.isSpeaking.value) return@forEach

                    _lastWarningLevels[trackId] = level


                    val label = classDictionary[result.classId] ?: "미확인"
                    val type = when (result.classId) {
                        0 -> "PERSON_DETECTED"
                        1 -> "BICYCLE_DETECTED"
                        else -> "CAR_DETECTED"
                    }

                    val command = DetectionCommand(
                        type = type,
                        level = level,
                        message = label
                    )

                    websocketRepository.sendDetectionCommand(command)
                    DetectionNoticeStateManager.updateNoticeState(type, level)
                }
            }
        }
    }
}
