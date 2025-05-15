package com.a303.helpmet.di

import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val navigationModule = module {
    viewModel { NavigationViewModel(
        deviceRepository = get(),
        directionSocketRepository = get()
    ) }
}
