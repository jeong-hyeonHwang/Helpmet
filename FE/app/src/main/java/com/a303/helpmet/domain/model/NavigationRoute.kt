package com.a303.helpmet.domain.model

data class NavigationRoute(
    val distanceM: Double,
    val estimatedTimeSec: Int,
    val segments: List<RouteSegment>,
    val instructions: List<Instruction>
)
