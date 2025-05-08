package com.a303.helpmet.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 구간별 경로(segments)
@Serializable
data class RouteSegmentDto(
    @SerialName("from")
    val from: LatLngDto,

    @SerialName("to")
    val to: LatLngDto,

    @SerialName("is_cycleway")
    val isCycle: Boolean,

    @SerialName("distance_m")
    val distance: Double
)