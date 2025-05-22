package com.a303.helpmet.presentation.feature.navigation.usecase

import com.a303.helpmet.presentation.feature.navigation.RADIUS
import com.a303.helpmet.presentation.feature.navigation.TRI_F
import com.a303.helpmet.presentation.feature.navigation.TRI_S
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.shape.DotPoints
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.Polygon
import com.kakao.vectormap.shape.PolygonOptions
import com.kakao.vectormap.shape.PolygonStyle
import com.kakao.vectormap.shape.ShapeLayer

class UpdateUserPositionShapesUseCase {
    private var circlePolygon: Polygon? = null
    private var trianglePolygon: Polygon? = null

    operator fun invoke(
        layer: ShapeLayer,
        position: LatLng,
        heading: Float,
        radius: Float = RADIUS,
        triF: Float = TRI_F,
        triS: Float = TRI_S
    ) {
        if (circlePolygon == null && trianglePolygon == null) {
            layer.removeAll()

            circlePolygon = layer.addPolygon(
                PolygonOptions.from(
                    DotPoints.fromCircle(position, radius),
                    PolygonStyle.from(0x5533B5E5)
                )
            )
            trianglePolygon = layer.addPolygon(
                PolygonOptions.from(
                    MapPoints.fromLatLng(makeTriPoints(position, heading, triF, triS)),
                    PolygonStyle.from(0xAAFF0000.toInt())
                )
            )
        } else {
            // 위치 업데이트
            circlePolygon?.setPosition(position)
            // 방향 업데이트
            trianglePolygon?.changeMapPoints(
                listOf(MapPoints.fromLatLng(makeTriPoints(position, heading, triF, triS)))
            )
        }
    }

    private fun makeTriPoints(
        center: LatLng,
        heading: Float,
        triF: Float = TRI_F,
        triS: Float = TRI_S
    ): List<LatLng> {
        val forward = offset(center, triF, heading)
        val left    = offset(center, triS, heading - 15)
        val right   = offset(center, triS, heading + 15)
        return listOf(forward, left, right, forward)
    }

    private fun offset(o: LatLng, d: Float, hdg: Float): LatLng {
        val rad = Math.toRadians(hdg.toDouble())
        val dy  = d * Math.cos(rad)
        val dx  = d * Math.sin(rad)
        val dLat = dy / 111000.0
        val dLng = dx / (111000.0 * Math.cos(Math.toRadians(o.latitude)))
        return LatLng.from(o.latitude + dLat, o.longitude + dLng)
    }
}