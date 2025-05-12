package com.a303.helpmet.presentation.feature.voiceinteraction

sealed class VoicePromptContext {
    object None : VoicePromptContext()
    object Restroom : VoicePromptContext()
    object Parking : VoicePromptContext()
    object EndGuide : VoicePromptContext()
}
