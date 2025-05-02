package com.a303.helpmet.presentation.model

enum class HelmetConnectionState {
    Idle, // 기본 상태(다이얼로그 뜨지 않음)
    Searching, // 헬멧을 찾는 중
    Found, // 헬멧 찾음
    Connecting, // 헬멧 연결 중
    Success // 연결 성공(메시지 띄움)
}