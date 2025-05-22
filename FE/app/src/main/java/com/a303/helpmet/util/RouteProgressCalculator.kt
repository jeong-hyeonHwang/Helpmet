package com.a303.helpmet.util

import com.kakao.vectormap.LatLng
import kotlin.math.*

object RouteProgressCalculator {

    fun calculateProgressAndSnappedPoint(
        user: LatLng,
        route: List<LatLng>,
        previousProgress: Float = 0f
    ): Pair<Float, LatLng> {
        if (route.size < 2) return previousProgress to user

        var total = 0.0
        var progress = 0.0
        var closestDist = Double.MAX_VALUE
        var snappedPoint = user

        for (i in 0 until route.size - 1) {
            val a = route[i]
            val b = route[i + 1]
            val segLen = distance(a, b)
            total += segLen

            val (dist, snapped) = distanceToSegmentWithSnap(user, a, b)
            if (dist < closestDist) {
                closestDist = dist
                progress = total - segLen + distance(a, snapped)
                snappedPoint = snapped
            }
        }

        val progressRatio = (progress / total).toFloat().coerceIn(0f, 1f)
        return max(progressRatio, previousProgress) to snappedPoint
    }

    fun distance(a: LatLng, b: LatLng): Double {
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val r = 6371000.0 // 지구 반지름
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)

        val aVal = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(aVal), sqrt(1 - aVal))
        return r * c
    }

    private fun distanceToSegmentWithSnap(p: LatLng, a: LatLng, b: LatLng): Pair<Double, LatLng> {
        val px = p.longitude
        val py = p.latitude
        val ax = a.longitude
        val ay = a.latitude
        val bx = b.longitude
        val by = b.latitude

        val dx = bx - ax
        val dy = by - ay
        if (dx == 0.0 && dy == 0.0) return distance(p, a) to a

        val t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)
        val clampedT = t.coerceIn(0.0, 1.0)
        val projX = ax + clampedT * dx
        val projY = ay + clampedT * dy
        val snapped = LatLng.from(projY, projX)
        return distance(p, snapped) to snapped
    }
}
