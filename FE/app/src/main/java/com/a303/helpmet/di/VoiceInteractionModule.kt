package com.a303.helpmet.di

import com.a303.helpmet.domain.usecase.SendDirectionCommandUseCase
import com.a303.helpmet.presentation.feature.voiceinteraction.VoiceInteractViewModel
import com.a303.helpmet.presentation.feature.voiceinteraction.usecase.EndGuideUseCase
import com.a303.helpmet.presentation.feature.voiceinteraction.usecase.NavigateToRentalStationUseCase
import com.a303.helpmet.presentation.feature.voiceinteraction.usecase.NavigateToRestroomUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val voiceInteractionModule = module {
    viewModel {
        VoiceInteractViewModel(
            application = get(),
            navigateToRestroom = get(),
            navigateToRental = get(),
            endGuide = get(),
            sendDirectionCommandUseCase = get()
        )
    }

    factory { NavigateToRestroomUseCase() }
    factory { NavigateToRentalStationUseCase() }
    factory { EndGuideUseCase() }

    single { SendDirectionCommandUseCase(get()) }

}

