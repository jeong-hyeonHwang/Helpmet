package com.a303.helpmet.presentation.feature.voiceinteraction

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.R
import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import com.a303.helpmet.presentation.feature.voiceinteraction.sound.TickSoundManager
import com.a303.helpmet.presentation.feature.voiceinteraction.usecase.*
import com.a303.helpmet.presentation.feature.voiceinteraction.util.UserReplyResponse
import com.a303.helpmet.presentation.model.DirectionStateManager
import com.a303.helpmet.presentation.model.VoiceCommand
import com.a303.helpmet.util.handler.VoiceInteractionHandler

class VoiceInteractViewModel(
    application: Application,
    private val navigateToRestroom: NavigateToRestroomUseCase,
    private val navigateToRental: NavigateToRentalStationUseCase,
    private val endGuide: EndGuideUseCase,
) : AndroidViewModel(application) {

    private val tickSoundManager = TickSoundManager(
        context = application.applicationContext,
        directionState = DirectionStateManager.directionState,
        scope = viewModelScope
    )

    private val playTurnSignal = PlayTurnSignalSoundUseCase(
        tickSoundManager = tickSoundManager,
//        updateDirectionState = { navigationViewModel.updateDirectionState(it) }
        updateDirectionState = { DirectionStateManager.update(it) }
    )

    private var promptContext: VoicePromptContext = VoicePromptContext.None
    private val voiceHandler = VoiceInteractionHandler(application.applicationContext)
    private val context = getApplication<Application>()

    init {
        voiceHandler.updateRecognitionCallback { text -> handleVoiceInput(text) }
        voiceHandler.startListening()
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
        val command = VoiceCommand.fromText(text).firstOrNull()
        when (command) {
            VoiceCommand.TURN_LEFT -> {
                speak(context.getString(R.string.voice_turn_left_signal))
                Handler(Looper.getMainLooper()).postDelayed({
                    playTurnSignal(DirectionState.Left)
                }, 1000)
            }
            VoiceCommand.TURN_RIGHT -> {
                speak(context.getString(R.string.voice_turn_right_signal))
                Handler(Looper.getMainLooper()).postDelayed({
                    playTurnSignal(DirectionState.Right)
                }, 1000)
            }
            VoiceCommand.END_TURN_SIGNAL -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    playTurnSignal(DirectionState.None)
                }, 100)
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
        voiceHandler.startListening()
    }

    private fun speak(text: String) {
        voiceHandler.speak(text)
    }

    fun startListening() {
        voiceHandler.startListening()
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
