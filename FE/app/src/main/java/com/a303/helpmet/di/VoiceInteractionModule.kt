package com.a303.helpmet.di

import com.a303.helpmet.domain.usecase.SendDirectionCommandUseCase
import com.a303.helpmet.presentation.feature.navigation.viewmodel.RouteViewModel
import com.a303.helpmet.presentation.feature.voiceinteraction.VoiceInteractViewModel
import com.a303.helpmet.presentation.feature.voiceinteraction.usecase.EndGuideUseCase
import com.a303.helpmet.presentation.feature.voiceinteraction.usecase.NavigateToRentalStationUseCase
import com.a303.helpmet.presentation.feature.voiceinteraction.usecase.NavigateToPlaceUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val voiceInteractionModule = module {
    viewModel {
        VoiceInteractViewModel(
            application = get(),
            navigateToPlace = get(),
            endGuide = get(),
            sendDirectionCommandUseCase = get(),
        )
    }

    factory { NavigateToPlaceUseCase(get()) }
    factory { EndGuideUseCase() }
    single { SendDirectionCommandUseCase(get()) }

}

