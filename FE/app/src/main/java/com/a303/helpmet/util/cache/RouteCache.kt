package com.a303.helpmet.util.cache

import com.kakao.vectormap.route.RouteLineOptions

object RouteCache {
    private var cachedRoute: RouteLineOptions? = null

    fun set(route: RouteLineOptions) {
        cachedRoute = route
    }

    fun get(): RouteLineOptions? = cachedRoute

    fun clear() {
        cachedRoute = null
    }
}