package com.a303.helpmet.di

import com.a303.helpmet.presentation.feature.navigation.viewmodel.DetectionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val detectionModule = module {
    viewModel { DetectionViewModel(
        application= get(),
        websocketRepository = get()) }
}
