package com.a303.helpmet.presentation.state.detection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


object PiAccessManager {
    private val _isSpeakingState = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeakingState

    private val _isAccessState = MutableStateFlow(false);
    val isAccessible: StateFlow<Boolean> = _isAccessState

    fun updateSpeakingState(isSpeaking: Boolean){
        _isSpeakingState.value = isSpeaking;
    }
}
