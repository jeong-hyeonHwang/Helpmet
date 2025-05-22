package com.a303.helpmet.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 위도·경도 공통 DTO
@Serializable
data class LatLngDto(
    @SerialName("lat")
    val lat: Double,

    @SerialName("lon")
    val lng: Double
)
