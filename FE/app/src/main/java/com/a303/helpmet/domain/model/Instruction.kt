package com.a303.helpmet.domain.model

data class Instruction(
    val index: Int,
    val location: LatLng,
    val distanceM: Double?,         // 구간 이동 거리
    val distanceToHereM: Double?,   // 남은 거리
    val action: Action,
    val message: String
)