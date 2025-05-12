package com.a303.helpmet.di

import com.a303.helpmet.presentation.feature.navigation.viewmodel.RouteViewModel
import com.a303.helpmet.presentation.feature.preride.PreRideViewModel
import com.a303.helpmet.presentation.feature.preride.UserPositionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val prerideModule = module {
    viewModel { PreRideViewModel(service = get()) }
    viewModel { UserPositionViewModel() }
    viewModel { RouteViewModel() }
}
