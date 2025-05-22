package com.a303.helpmet.domain.model

enum class Action(val threshold: Double) {
    STRAIGHT(30.0),
    LEFT(15.0),
    RIGHT(15.0),
    CROSS(10.0)
}
