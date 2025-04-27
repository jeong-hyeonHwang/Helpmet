package com.a303.helpmet.presentation.feature.helmetcheck

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HelmetCheckViewModel : ViewModel() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    fun checkConnection() {
        // TODO: 실제 블루투스 헬멧 연결 확인 로직
        _isConnected.value = true  // 임시: 항상 연결된 것으로 처리
    }
}