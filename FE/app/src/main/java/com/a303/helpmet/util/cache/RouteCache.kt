package com.a303.helpmet.util.cache

import com.a303.helpmet.presentation.model.InstructionUi
import com.a303.helpmet.presentation.model.RouteInfo
import com.kakao.vectormap.route.RouteLineOptions

object RouteCache {
    private var cachedRoute: RouteLineOptions? = null
    private var cachedInstructionList: List<InstructionUi>? = null
    private var cachedRouteInfo: RouteInfo? = null

    fun set(route: RouteLineOptions, instructionList: List<InstructionUi>, routeInfo: RouteInfo) {
        cachedRoute = route
        cachedInstructionList = instructionList
        cachedRouteInfo = routeInfo
    }

    fun getRoute(): RouteLineOptions? = cachedRoute

    fun getInstructionList(): List<InstructionUi>? = cachedInstructionList

    fun getRouteInfo(): RouteInfo? = cachedRouteInfo

    fun clear() {
        cachedRoute = null
        cachedRouteInfo = null
    }
}