package com.a303.helpmet.presentation.model

data class NavigationUiState(
    val startLocation: LatLngUi,
    val endLocation: LatLngUi,
    val distanceText: String,      // ex. "0.8km"
    val timeText: String,          // ex. "3분"
    val segments: List<SegmentUi>,
    val instructions: List<InstructionUi>
)