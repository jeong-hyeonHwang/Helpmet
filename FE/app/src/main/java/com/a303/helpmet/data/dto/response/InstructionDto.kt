package com.a303.helpmet.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 안내 문구(Instructions)
@Serializable
data class InstructionDto(
    @SerialName("index")
    val index: Int,

    @SerialName("location")
    val location: LatLngDto,

    @SerialName("distance_m")
    val distance: Double,

    @SerialName("action")
    val action: String,

    @SerialName("message")
    val message: String
)