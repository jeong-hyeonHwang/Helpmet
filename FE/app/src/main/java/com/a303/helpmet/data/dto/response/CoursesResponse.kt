package com.a303.helpmet.data.dto.response

import kotlinx.serialization.Serializable

// 임시 데이터 dto
@Serializable
data class CourseResponse(
    val courseNumber: Int,
    val duration: Int,        // 단위: 분
    val distanceKm: Float,    // 단위: km
    val startStation: String,
    val endStation: String,
    val navId: Int
)

