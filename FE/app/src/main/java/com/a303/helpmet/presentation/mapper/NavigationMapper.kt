package com.a303.helpmet.presentation.mapper

import android.annotation.SuppressLint
import android.content.Context
import com.a303.helpmet.R
import com.a303.helpmet.domain.model.Instruction
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.a303.helpmet.domain.model.NavigationRoute
import com.a303.helpmet.domain.model.RouteSegment
import com.a303.helpmet.presentation.model.InstructionUi
import com.a303.helpmet.presentation.model.LatLngUi
import com.a303.helpmet.presentation.model.RouteInfo
import com.a303.helpmet.presentation.model.SegmentUi

fun com.a303.helpmet.domain.model.LatLng.toUi() = LatLngUi(
    latitude = lat,
    longitude = lng
)

fun RouteSegment.toUi() = SegmentUi(
    from    = from.toUi(),
    to      = to.toUi(),
    isCycle = isCycle
)

fun Instruction.toUi() = InstructionUi(
    index    = index,
    location = location.toUi(),
    message  = message
)

@SuppressLint("DefaultLocale")
fun NavigationRoute.toRouteInfo(index: Int): RouteInfo = RouteInfo(
    routeId = index,
    duration = estimatedTimeSec / 60,
    distanceKm = distance / 1000.0,
    startLocationName = "시작지점입니다", // MARK: 테스트 데이터셋
    startLocation = segments.first().from.toUi(),
    endLocationName = "끝지점입니다", // MARK: 테스트 데이터셋
    endLocation = segments.last().to.toUi())

/**
 * NavigationRoute(도메인) → KakaoVectorMap RouteLineOptions 로 변환
 */
fun NavigationRoute.toRouteLineOptions(context: Context): RouteLineOptions {
    // 1) Domain 세그먼트 하나하나를 RouteLineSegment 로 변환
    val kakaoSegments = segments.map { domainSeg ->
        // Kakao LatLng 배열
        val pts = arrayOf(
            LatLng.from(domainSeg.from.lat, domainSeg.from.lng),
            LatLng.from(domainSeg.to.lat,   domainSeg.to.lng)
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
