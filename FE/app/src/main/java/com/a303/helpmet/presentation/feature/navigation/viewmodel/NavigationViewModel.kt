package com.a303.helpmet.presentation.feature.navigation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NavigationViewModel() : ViewModel()  {
//    private val _progress = MutableStateFlow(0)
//    val progress: StateFlow<Int> = _progress
//
//    fun startNavigation() {
//        // TODO: 실제 내비 로직
//        _progress.value = 100
//    }

    private val _isActiveStreamingView = MutableStateFlow(true)
    val isActiveStreamingView: StateFlow<Boolean> = _isActiveStreamingView

    // 토글 함수
    fun toggleStreaming() {
        _isActiveStreamingView.value = !_isActiveStreamingView.value
    }
}