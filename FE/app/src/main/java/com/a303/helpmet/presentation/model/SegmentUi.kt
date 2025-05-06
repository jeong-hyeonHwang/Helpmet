package com.a303.helpmet.presentation.model

data class SegmentUi(
    val from: LatLngUi,
    val to: LatLngUi,
    val isCycle: Boolean
)