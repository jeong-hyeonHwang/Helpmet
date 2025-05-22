package com.a303.helpmet.presentation.model

data class RouteInfo(
    var routeId: Int = -1,
    val duration: Int = 0,
    val distanceKm: Double = 0.0,
    val startLocationName: String = "",
    val endLocationName: String = "",
    val startLocation: LatLngUi = LatLngUi(0.0,0.0),
    val endLocation: LatLngUi = LatLngUi(0.0,0.0)
)
