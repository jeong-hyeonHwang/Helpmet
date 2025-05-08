package com.a303.helpmet.di

import com.a303.helpmet.presentation.feature.voiceguide.VoiceGuideViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val voiceGuideModule = module {
    viewModel { VoiceGuideViewModel() }
}