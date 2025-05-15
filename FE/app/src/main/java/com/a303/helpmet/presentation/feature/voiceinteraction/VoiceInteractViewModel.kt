package com.a303.helpmet.presentation.feature.voiceinteraction

import android.app.Application
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VoiceInteractViewModel(
    application: Application,
    private val navigateToRestroom: NavigateToRestroomUseCase,
    private val navigateToRental: NavigateToRentalStationUseCase,
    private val endGuide: EndGuideUseCase,
    private val sendDirectionCommandUseCase: SendDirectionCommandUseCase
) : AndroidViewModel(application) {

    private val tickSoundManager = TickSoundManager(
        context = application.applicationContext,
        directionState = DirectionStateManager.directionState,
        scope = viewModelScope
    )

    private val playTurnSignal = PlayTurnSignalSoundUseCase(
        tickSoundManager = tickSoundManager,
        updateDirectionState = { DirectionStateManager.update(it) }
    )

    private val turnOnOffSignal = TurnOnOffSignalUseCase(
        speak = { text -> speak(text) },
        playTurnSignal = { direction -> playTurnSignal(direction) },
        sendCommand = { command -> sendDirectionCommandUseCase(command) }
    )

    private var promptContext: VoicePromptContext = VoicePromptContext.None
    private val voiceHandler = VoiceInteractionHandler(application.applicationContext)
    private val context = getApplication<Application>()

    private val _isVoiceReady = MutableStateFlow(false)
    val isVoiceReady: StateFlow<Boolean> get() = _isVoiceReady


    init {
        voiceHandler.updateRecognitionCallback { text -> handleVoiceInput(text) }
        Log.d("VoiceHandler", "생성됨")
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

    private fun handleVoiceInput(text: String) {
        // 후속 응답 처리
        if (promptContext != VoicePromptContext.None) {
            when {
                UserReplyResponse.positiveResponses.any { it in text } -> {
                    when (promptContext) {
                        VoicePromptContext.Restroom -> {
                            speak(context.getString(R.string.voice_reroute_message))
                            navigateToRestroom()
                        }
                        VoicePromptContext.Parking,
                        VoicePromptContext.EndGuide -> {
                            speak(context.getString(R.string.voice_reroute_message))
                            navigateToRental()
                        }
                        VoicePromptContext.None -> {}
                    }
                }
                UserReplyResponse.negativeResponses.any { it in text } -> {
                    if (promptContext is VoicePromptContext.EndGuide) {
                        speak(context.getString(R.string.voice_guide_end_message))
                        endGuide()
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
                    VoiceCommand.TURN_LEFT -> {
                        turnOnOffSignal(DirectionState.Left)
                    }
                    VoiceCommand.TURN_RIGHT -> {
                        turnOnOffSignal(DirectionState.Right)
                    }
                    VoiceCommand.END_TURN_SIGNAL -> {
                        turnOnOffSignal(DirectionState.None)
                    }
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

    fun speak(text: String) {
        voiceHandler.speak(text) {
            voiceHandler.startListening() // TTS 끝나고 나면 STT 시작
        }
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
    }
}
