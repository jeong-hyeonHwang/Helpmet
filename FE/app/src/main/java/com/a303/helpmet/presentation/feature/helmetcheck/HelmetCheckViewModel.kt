package com.a303.helpmet.presentation.feature.helmetcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.presentation.model.HelmetConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HelmetCheckViewModel : ViewModel() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected // 헬멧의 최종 연결 상태

    private val _helmetName = MutableStateFlow("Helpmet-A303") // 임시 헬멧 명
    val helmetName: StateFlow<String> = _helmetName

    private val _connectionState = MutableStateFlow(HelmetConnectionState.Idle) // 헬멧 연결 상태(기본값: 안됨)
    val connectionState: StateFlow<HelmetConnectionState> = _connectionState

    fun checkConnection() {
        // TODO: 실제 블루투스 헬멧 연결 확인 로직

        // 임시: 연결된 상태인 경우 블루투스 연결 해제
        if(_isConnected.value == true) {
            _isConnected.value = false
        }else{
            _isConnected.value = true  // 임시: 항상 연결된 것으로 처리
        }
    }

    // 헬멧 찾는 중
    fun startSearch(){
        _connectionState.value = HelmetConnectionState.Searching
        viewModelScope.launch {
            delay(1000) // 임시: 1초 동안 로딩된 후에 찾음 상태로 변경
            _connectionState.value = HelmetConnectionState.Found
        }
    }

    // 헬멧에 진짜로 연결
    fun confirmConnection() {
        _connectionState.value = HelmetConnectionState.Connecting
        viewModelScope.launch {
            delay(1000) // 임시: 1초 후 연결되게끔
            _isConnected.value = true
            _connectionState.value = HelmetConnectionState.Success
            delay(1000) // 다이얼로그를 닫기 위해 초기 상태로 되돌림
            _connectionState.value = HelmetConnectionState.Idle
        }
    }

    // 헬멧에 연결하지 않기(=다이얼로그 닫기)
    fun cancelDialog() {
        _connectionState.value = HelmetConnectionState.Idle
        _isConnected.value = false
    }

    fun setHelmetName(name: String){
        _helmetName.value = name
    }

}