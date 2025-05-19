package com.a303.helpmet.presentation.feature.voiceinteraction

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.R
import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.domain.usecase.SendDirectionCommandUseCase
import com.a303.helpmet.presentation.feature.voiceinteraction.sound.TickSoundManager
import com.a303.helpmet.presentation.feature.voiceinteraction.usecase.*
import com.a303.helpmet.presentation.feature.voiceinteraction.util.UserReplyResponse
import com.a303.helpmet.presentation.state.DirectionStateManager
import com.a303.helpmet.presentation.model.VoiceCommand
import com.a303.helpmet.util.handler.VoiceInteractionHandler
import com.a303.helpmet.util.postPosition.appendAdverbialPostposition
import com.a303.helpmet.util.postPosition.appendObjectPostposition
import com.kakao.vectormap.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VoiceInteractViewModel(
    application: Application,
    private val navigateToPlace: NavigateToPlaceUseCase,
    private val sendDirectionCommandUseCase: SendDirectionCommandUseCase,
) : AndroidViewModel(application) {
    // 안내 종료 시 맵의 상태를 다 제거
    private val _onGuideEnd = MutableSharedFlow<Unit>()
    val onGuideEnd: SharedFlow<Unit> get() = _onGuideEnd

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun requestGuideEnd() {
        viewModelScope.launch {
            _onGuideEnd.emit(Unit)
        }
    }

    private val tickSoundManager = TickSoundManager(
        context = application.applicationContext,
        directionState = DirectionStateManager.directionState,
        scope = viewModelScope
    )

    private val playTurnSignal = PlayTurnSignalSoundUseCase(
        tickSoundManager = tickSoundManager,
        updateDirectionState = { DirectionStateManager.update(it) }
    )

    val turnOnOffSignal = TurnOnOffSignalUseCase(
        speak = { text -> speak(text) },
        playTurnSignal = { direction -> playTurnSignal(direction) },
        sendCommand = { command -> sendDirectionCommandUseCase(command) }
    )

    private var promptContext: VoicePromptContext = VoicePromptContext.None
    private val voiceHandler = VoiceInteractionHandler(application.applicationContext)
    private val context = getApplication<Application>()

    private val _isVoiceReady = MutableStateFlow(false)
    val isVoiceReady: StateFlow<Boolean> get() = _isVoiceReady

    private var currentPosition: LatLng? = null

    fun updatePosition(pos: LatLng) {
        currentPosition = pos
    }

    init {
        voiceHandler.updateRecognitionCallback { text -> handleVoiceInput(text) }
        viewModelScope.launch {
            while (!_isVoiceReady.value) {
                if (voiceHandler.isTtsReady) {
                    _isVoiceReady.value = true
                    break
                }
                delay(50)
            }
        }
    }

    fun onReturnAlertReceived(){
        promptContext = VoicePromptContext.Parking
        speak(context.getString(R.string.voice_return_alarm_message))
    }

    private var onRouteUpdate: ((NavigateRouteResult) -> Unit)? = null

    fun setOnRouteUpdateListener(callback: (NavigateRouteResult) -> Unit) {
        this.onRouteUpdate = callback
    }

    private fun navigateToPlaceIfReady(placeType: String) {
        speak(context.getString(R.string.voice_reroute_message))
        val pos = currentPosition
        if (pos == null) {
            speak("사용자 위치를 파악할 수 없습니다. 잠시 후 다시 시도해주세요.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = navigateToPlace.invoke(context, pos, placeType)
            _isLoading.value = false
            if (result != null) {
                onRouteUpdate?.invoke(result)
                speak("${appendAdverbialPostposition(placeTypeToKor(placeType))} 안내를 시작합니다.")
            } else {
                speak("${appendObjectPostposition(placeTypeToKor(placeType))} 못찾았습니다. 잠시 후 다시 시도해주세요.")
            }
        }
    }

    private fun placeTypeToKor(placeType: String):String = when(placeType){
        "toilet" -> "주변 화장실"
        "rental" -> "주변 대여소"
        else -> ""
    }

    fun setEndGuideContext(){
        turnOnOffSignal(DirectionState.None)
        promptContext = VoicePromptContext.EndGuide
        speak(context.getString(R.string.voice_arrival_destination_message))
    }

    private fun handleVoiceInput(text: String) {
        // 후속 응답 처리
        if (promptContext != VoicePromptContext.None) {
            when {
                UserReplyResponse.positiveResponses.any { it in text } -> {
                    when (promptContext) {
                        VoicePromptContext.Restroom -> {
                            navigateToPlaceIfReady("toilet")
                        }
                        VoicePromptContext.Parking,
                        VoicePromptContext.EndGuide -> {
                            navigateToPlaceIfReady("rental")
                        }
                        VoicePromptContext.None -> {}
                    }
                }
                UserReplyResponse.negativeResponses.any { it in text } -> {
                    if (promptContext is VoicePromptContext.EndGuide) {
                        requestGuideEnd()
                        speak(context.getString(R.string.voice_guide_end_message))
                    }
                }
            }
            promptContext = VoicePromptContext.None
            voiceHandler.startListening()
            return
        }

        // 명령 인식
        val commands = VoiceCommand.fromText(text)
        when{
            commands.size <= 1 -> {
                val command = commands.firstOrNull()
                when (command) {
                    VoiceCommand.TURN_LEFT -> turnOnOffSignal(DirectionState.Left)
                    VoiceCommand.TURN_RIGHT -> turnOnOffSignal(DirectionState.Right)
                    VoiceCommand.END_TURN_SIGNAL -> turnOnOffSignal(DirectionState.None)

                    VoiceCommand.RESTROOM -> {
                        promptContext = VoicePromptContext.Restroom
                        speak(context.getString(R.string.voice_prompt_restroom))
                    }
                    VoiceCommand.PARKING_ZONE -> {
                        promptContext = VoicePromptContext.Parking
                        speak(context.getString(R.string.voice_prompt_rental))
                    }
                    VoiceCommand.END_GUIDE -> {
                        promptContext = VoicePromptContext.EndGuide
                        speak(context.getString(R.string.voice_prompt_rental))
                    }
                    else -> { }
                }
            }
            else -> {
                val label = commands.joinToString(", ") { it.korLabel }
                speak("${label} 중 어떤 걸 도와드릴까요?")
            }
        }
        voiceHandler.startListening()
    }

    fun speak(text: String, autoStartListening: Boolean = true) {
        voiceHandler.speak(text, autoStartListening)
    }

    fun startListening() {
        voiceHandler.startListening()
    }

    fun stopListening(){
        voiceHandler.destroy()
    }

    fun notifyPermissionMissing() {
        speak(context.getString(R.string.voice_permission_required))
    }

    override fun onCleared() {
        super.onCleared()
        DirectionStateManager.update(DirectionState.None)
        voiceHandler.destroy()
        tickSoundManager.releaseAll()
        onRouteUpdate = null
    }
}
