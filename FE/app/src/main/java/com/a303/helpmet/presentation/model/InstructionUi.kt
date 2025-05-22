package com.a303.helpmet.presentation.model

import com.a303.helpmet.domain.model.Action

data class InstructionUi(
    val index: Int,
    val location: LatLngUi,
    val action: Action,
    val message: String,
    val isSpoken: Boolean = false
)