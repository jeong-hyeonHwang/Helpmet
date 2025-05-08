package com.a303.helpmet.domain.model

data class RouteSegment(
    val from: LatLng,
    val to: LatLng,
    val isCycle: Boolean,
    val distance: Double
)