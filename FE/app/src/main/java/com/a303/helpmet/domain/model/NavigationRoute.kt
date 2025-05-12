package com.a303.helpmet.domain.model

data class NavigationRoute(
    // TODO: Location 관련 값 DTO 변경 시 Data - Domain - Presentation 전체 반영
    val distance: Double,
    val estimatedTimeSec: Int,
    val segments: List<RouteSegment>,
    val instructions: List<Instruction>
)
