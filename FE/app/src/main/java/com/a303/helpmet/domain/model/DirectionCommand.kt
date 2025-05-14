package com.a303.helpmet.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DirectionCommand(
    val type: String,      // "turn_left", "turn_right", "turn_off"
    val command: String    // "start", "stop", "both", "left"
)
