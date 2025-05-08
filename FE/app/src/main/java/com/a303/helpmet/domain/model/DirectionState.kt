package com.a303.helpmet.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DirectionState {
    @SerialName("right") Right,
    @SerialName("left") Left,
    @SerialName("none") None
}