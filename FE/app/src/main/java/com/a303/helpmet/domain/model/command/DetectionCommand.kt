package com.a303.helpmet.domain.model.command

import kotlinx.serialization.Serializable


@Serializable
data class DetectionCommand(
    val type: String, // DETECTION_PERSON, DETECTION_CAR, DETECTION_BICYCLE
    val level: Int, // 1,2
    val message: String
)

