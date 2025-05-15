package com.a303.helpmet.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 전체 경로 응답 DTO
@Serializable
data class NavigationResponseDto(
    @SerialName("distance_m")
    val distance: Double,

    @SerialName("estimated_time_sec")
    val estimatedTimeSec: Int,

    @SerialName("start_addr")
    val startAddr: String,

    @SerialName("end_addr")
    val endAddr: String,

    @SerialName("route")
    val route: List<RouteSegmentDto>,

    @SerialName("instructions")
    val instructions: List<InstructionDto>
)