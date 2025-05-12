package com.a303.helpmet.presentation.model

data class RouteInfo(
    var routeId: Int,
    val duration: Int,
    val distanceKm: Double,
    val startLocationName: String,
    val startLocation: LatLngUi,
    val endLocation: LatLngUi,
    val endLocationName: String
)
