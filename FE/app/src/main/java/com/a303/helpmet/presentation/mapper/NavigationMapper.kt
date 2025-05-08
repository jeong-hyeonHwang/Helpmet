package com.a303.helpmet.presentation.mapper

import android.content.Context
import com.a303.helpmet.R
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.a303.helpmet.domain.model.NavigationRoute

/**
 * NavigationRoute(도메인) → KakaoVectorMap RouteLineOptions 로 변환
 */
fun NavigationRoute.toRouteLineOptions(context: Context): RouteLineOptions {
    // 1) Domain 세그먼트 하나하나를 RouteLineSegment 로 변환
    val kakaoSegments = segments.map { domainSeg ->
        // Kakao LatLng 배열
        val pts = arrayOf(
            LatLng.from(domainSeg.from.lat, domainSeg.from.lon),
            LatLng.from(domainSeg.to.lat,   domainSeg.to.lon)
        )
        // 스타일 리소스 분기 (예: 자전거도로 vs 보행자)
        val styleRes = if (domainSeg.isCycle)
            R.style.BlueRouteLineStyle
        else
            R.style.RedRouteLineStyle

        // RouteLineStyle.from(context, styleRes) → RouteLineSegment 생성
        RouteLineSegment.from(pts, RouteLineStyle.from(context, styleRes))
    }

    // 2) RouteLineOptions 생성
    return RouteLineOptions.from(kakaoSegments)
}
