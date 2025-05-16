package com.a303.helpmet.di

import android.content.Context
import com.a303.helpmet.data.network.RetrofitProvider
import com.a303.helpmet.presentation.feature.navigation.viewmodel.RouteViewModel
import com.a303.helpmet.presentation.feature.preride.PreRideViewModel
import com.a303.helpmet.presentation.feature.preride.UserPositionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val prerideModule = module {
    viewModel {
        PreRideViewModel(
            retrofitProvider = get { parametersOf(get<Context>()) }, // Context param 전달
            getCellularNetworkUseCase = get()
        )
    }
    viewModel { UserPositionViewModel() }
    viewModel { RouteViewModel() }
}
