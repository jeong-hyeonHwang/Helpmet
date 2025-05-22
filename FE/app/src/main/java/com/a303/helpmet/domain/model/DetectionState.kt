package com.a303.helpmet.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DetectionNoticeState {
    @SerialName("default") Default,
    @SerialName("caution") Caution,
    @SerialName("danger") Danger,
}


interface Displayable {
    fun toKorean(): String
}

@Serializable
enum class DetectedObjectState : Displayable {
    @SerialName("PERSON_DETECTED") Person,
    @SerialName("BICYCLE_DETECTED") Bicycle,
    @SerialName("CAR_DETECTED") Car,
    @SerialName("NULL") Default;

    override fun toKorean(): String = when (this) {
        Person   -> "보행자"
        Bicycle  -> "자전거"
        Car      -> "자동차"
        Default  -> "없음"
    }
}
