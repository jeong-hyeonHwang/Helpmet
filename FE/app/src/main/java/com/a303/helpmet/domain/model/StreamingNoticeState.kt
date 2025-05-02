package com.a303.helpmet.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StreamingNoticeState {
    @SerialName("default") Default,
    @SerialName("caution") Caution,
    @SerialName("danger") Danger,
}