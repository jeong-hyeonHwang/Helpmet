package com.a303.helpmet.util.cache

import com.a303.helpmet.presentation.model.RouteInfo
import com.kakao.vectormap.route.RouteLineOptions

object RouteCache {
    private var cachedRoute: RouteLineOptions? = null
    private var cachedRouteInfo: RouteInfo? = null

    fun set(route: RouteLineOptions, routeInfo: RouteInfo) {
        cachedRoute = route
        cachedRouteInfo = routeInfo
    }

    fun getRoute(): RouteLineOptions? = cachedRoute

    fun getRouteInfo(): RouteInfo? = cachedRouteInfo

    fun clear() {
        cachedRoute = null
        cachedRouteInfo = null
    }
}