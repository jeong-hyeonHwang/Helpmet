package com.a303.helpmet.domain.model

data class Instruction(
    val index: Int,
    val location: LatLng,
    val distance: Double,         // 구간 이동 거리
    val action: Action,
    val message: String
)