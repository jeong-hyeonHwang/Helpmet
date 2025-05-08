package com.a303.helpmet.presentation.feature.preride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.a303.helpmet.data.service.NavigationService

class MapViewModelFactory(
    private val service: NavigationService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreRideViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PreRideViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}