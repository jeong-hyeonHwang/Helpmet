package com.a303.helpmet.di

import com.a303.helpmet.domain.usecase.SendDirectionCommandUseCase
import com.a303.helpmet.presentation.feature.voiceinteraction.VoiceInteractViewModel
import com.a303.helpmet.presentation.feature.voiceinteraction.usecase.NavigateToPlaceUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val voiceInteractionModule = module {
    viewModel {
        VoiceInteractViewModel(
            application = get(),
            navigateToPlace = get(),
            sendDirectionCommandUseCase = get(),
        )
    }

    factory { NavigateToPlaceUseCase(get()) }
    single { SendDirectionCommandUseCase(get()) }

}

