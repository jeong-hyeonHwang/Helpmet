package com.a303.helpmet.presentation.state.detection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


object DetectionVoiceManager {
    private val _isSpeakingState = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeakingState

    fun updateSpeakingState(isSpeaking: Boolean){
        _isSpeakingState.value = isSpeaking;
    }
}

