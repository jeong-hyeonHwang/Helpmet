package com.a303.helpmet.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 안내 문구(Instructions)
@Serializable
data class InstructionDto(
    @SerialName("index")
    val index: Int,

    @SerialName("location")
    val location: LatLngDto,

    // distance_m 또는 distance_to_here_m 중 하나만 올 수 있으므로 nullable 처리
    @SerialName("distance_m")
    val distanceM: Double? = null,

    @SerialName("distance_to_here_m")
    val distanceToHereM: Double? = null,

    @SerialName("action")
    val action: String,

    @SerialName("message")
    val message: String
)