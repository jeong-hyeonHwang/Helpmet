package com.a303.helpmet.presentation.feature

sealed class NavRoutes(val route: String) {
    object HelmetCheck    : NavRoutes("helmet_check")
    object PreRide        : NavRoutes("pre_ride")
    object Navigation     : NavRoutes("navigation/{courseId}") {
        fun createRoute(courseId: String) = "navigation/$courseId"
    }
}
