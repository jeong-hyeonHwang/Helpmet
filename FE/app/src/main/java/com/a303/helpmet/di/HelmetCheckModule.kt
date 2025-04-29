package com.a303.helpmet.di

import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val helmetCheckModule = module {
    viewModel { HelmetCheckViewModel() }
}
