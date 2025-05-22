package com.a303.helpmet.util

import com.a303.helpmet.presentation.model.LatLngUi
import com.kakao.vectormap.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object GeoUtils {
    fun calculateBearing(from: LatLng, to: LatLngUi): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    fun normalizeAngle(deg: Double): Double {
        var angle = deg % 360.0
        if (angle < -180.0) angle += 360.0
        if (angle > 180.0) angle -= 360.0
        return angle
    }
}
