package com.a303.helpmet.domain.model

data class NavigationRoute(
    val distance: Double,
    val estimatedTimeSec: Int,
    val startAddress: String,
    val endAddress: String,
    val segments: List<RouteSegment>,
    val instructions: List<Instruction>
)
