package com.a303.helpmet.domain.extension

import android.util.Log
import com.a303.helpmet.domain.model.Action
import com.a303.helpmet.presentation.model.LatLngUi
import com.kakao.vectormap.LatLng
import kotlin.math.sqrt

fun LatLng.isNear(other: LatLngUi, threshold: Double = 20.0): Boolean {
    val latDiff = (latitude - other.latitude) * 111_000
    val lonDiff = (longitude - other.longitude) * 88_000
    val distance = sqrt(latDiff * latDiff + lonDiff * lonDiff)
    return distance <= threshold
}

fun LatLng.isApproaching(
    action: Action,
    target: LatLngUi,
): Boolean {
    val latDiff = (latitude - target.latitude) * 111_000
    val lonDiff = (longitude - target.longitude) * 88_000
    val distance = sqrt(latDiff * latDiff + lonDiff * lonDiff)

    return distance in 0.0..action.threshold
}
